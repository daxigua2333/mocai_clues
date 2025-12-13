package io.github.daxigua2333.mocai_clues.data.networks.snapshot;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.CodecNitriteAdapter;
import io.github.daxigua2333.mocai_clues.data.client.ClientDatabase;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class DbSnapshotReceiver {
    private DbSnapshotReceiver() {}

    private static volatile State state;

    private static final class State {
        final UUID snapshotId;
        final Path tempDbPath;
        final Nitrite tempDb;
//        final ObjectRepository<ClueObject> repo;
        final NitriteCollection coll;

        int received = 0;
        int expectedTotal = 0;

        State(UUID snapshotId, Path tempDbPath, Nitrite tempDb, NitriteCollection coll, int expectedTotal) {
            this.snapshotId = snapshotId;
            this.tempDbPath = tempDbPath;
            this.tempDb = tempDb;
            this.coll = coll;
            this.expectedTotal = expectedTotal;
        }
    }

    public static void handleStart(DbSnapshotPayloads.DbSnapshotStartPayload payload, IPayloadContext context) {
        try {
            closeStateQuietly();

            Path tempPath = ClientDatabase.tempDbPath(payload.snapshotId());
            Files.createDirectories(tempPath.getParent());
            Files.deleteIfExists(tempPath);

            Nitrite temp = ClientDatabase.openAt(tempPath);
//            ObjectRepository<ClueObject> repo = temp.getRepository(ClueObject.class);
            NitriteCollection repo = temp.getCollection("clue_object");
            repo.drop();

            state = new State(payload.snapshotId(), tempPath, temp, repo, payload.totalObjects());
        } catch (Throwable t) {
            context.disconnect(net.minecraft.network.chat.Component.literal("DB snapshot init failed: " + t.getMessage()));
        }
    }

    public static void handleChunk(DbSnapshotPayloads.DbSnapshotChunkPayload payload, IPayloadContext context) {
        State s = state;
        if (s == null || !s.snapshotId.equals(payload.snapshotId())) return;

        try {
            if (!payload.objects().isEmpty()) {
                CodecNitriteAdapter<ClueObject> adapter = new CodecNitriteAdapter<>(ClueObject.CODEC);
                for (ClueObject obj : payload.objects()) {
                    s.coll.insert(adapter.toDocument(obj));
                }
//                s.coll.insert(payload.objects().toArray(ClueObject[]::new));
                s.received += payload.objects().size();
            }

            if (payload.last()) {
                s.tempDb.close();

                // Replace the live client DB with the temp DB atomically-ish
                ClientDatabase.shutdown();

                Path livePath = ClientDatabase.liveDbPath();
                Files.createDirectories(livePath.getParent());

                try {
                    Files.move(s.tempDbPath, livePath,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                    );
                } catch (IOException atomicFail) {
                    Files.move(s.tempDbPath, livePath,
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }

                ClientDatabase.openLive();

                PacketDistributor.sendToServer(new DbSnapshotPayloads.DbSnapshotAckPayload(s.snapshotId));

                closeStateQuietly();
            }
        } catch (Throwable t) {
            context.disconnect(net.minecraft.network.chat.Component.literal("DB snapshot apply failed: " + t.getMessage()));
        }
    }

    private static void closeStateQuietly() {
        State s = state;
        state = null;
        if (s == null) return;
        try { s.tempDb.close(); } catch (Throwable ignored) {}
        try { Files.deleteIfExists(s.tempDbPath); } catch (Throwable ignored) {}
    }
}
