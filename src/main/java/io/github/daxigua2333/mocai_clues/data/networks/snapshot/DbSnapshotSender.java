package io.github.daxigua2333.mocai_clues.data.networks.snapshot;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.server.api.ServerDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class DbSnapshotSender {
    private DbSnapshotSender() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        sendFullSnapshot(player);
    }

//    TODO: use RegisterConfigurationTasksEvent
    //    @SubscribeEvent
//    public static void registerTasks(RegisterConfigurationTasksEvent event) {
//        event.register(new DbSyncConfigurationTask());
//    }
//    public record DbSyncConfigurationTask() implements ICustomConfigurationTask {
//        public static final ConfigurationTask.Type TYPE =
//                new ConfigurationTask.Type(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "db_sync"));
//
//        @Override
//        public void run(Consumer<CustomPacketPayload> sender) {
//            // 1) Build snapshot bytes
//            SnapshotBytes snap = DbSnapshotBuilder.buildGzippedJsonSnapshot();
//
//            // 2) Chunk and send
//            UUID snapshotId = UUID.randomUUID();
//            sender.accept(new DbSyncStartPayload(snapshotId, snap.chunks().size(), snap.rawBytes(), snap.crc32()));
//            for (int i = 0; i < snap.chunks().size(); i++) {
//                sender.accept(new DbSyncChunkPayload(snapshotId, i, snap.chunks().get(i)));
//            }
//
//            // DO NOT finish the task here; wait for DbSyncAckPayload from client.
//            // The docs show the server should finish the current task upon receiving the ack payload. :contentReference[oaicite:7]{index=7}
//        }
//
//        @Override
//        public ConfigurationTask.Type type() { return TYPE; }
//    }


    private static void sendFullSnapshot(ServerPlayer player) {
        List<ClueObject> all = ServerDataAccessor.queryAll(); // <- your Nitrite read (full copy)

        UUID snapshotId = UUID.randomUUID();

        int chunkSize = DbSnapshotPayloads.MAX_OBJECTS_PER_CHUNK;
        int totalObjects = all.size();
        int totalChunks = (totalObjects + chunkSize - 1) / chunkSize;

        PacketDistributor.sendToPlayer(player,
            new DbSnapshotPayloads.DbSnapshotStartPayload(snapshotId, totalObjects, totalChunks)
        );

        for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
            int from = chunkIndex * chunkSize;
            int to = Math.min(from + chunkSize, totalObjects);
            boolean last = (chunkIndex == totalChunks - 1);

            PacketDistributor.sendToPlayer(player,
                new DbSnapshotPayloads.DbSnapshotChunkPayload(snapshotId, chunkIndex, last, all.subList(from, to))
            );
        }
    }
}
