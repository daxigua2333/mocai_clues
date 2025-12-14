package io.github.daxigua2333.mocai_clues.data.sync.server;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectOpRecord;
import io.github.daxigua2333.mocai_clues.data.sync.NitriteMyObjectStore;
import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.github.daxigua2333.mocai_clues.data.sync.payload.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MyObjectSyncServer implements NitriteMyObjectStore.OpListener {

    private final NitriteMyObjectStore store;
    private final Map<UUID, Session> sessionsByPlayer = new ConcurrentHashMap<>();

    public MyObjectSyncServer(NitriteMyObjectStore store) {
        this.store = store;
        this.store.addListener(this);
    }

    public NitriteMyObjectStore store() {
        return store;
    }

    public void onPlayerLogout(ServerPlayer player) {
        if (player == null) return;
        sessionsByPlayer.remove(player.getUUID());
    }

    public Session getOrCreateSession(ServerPlayer player) {
        return sessionsByPlayer.compute(player.getUUID(), (k, existing) -> {
            if (existing == null || existing.player != player) {
                return new Session(player, UUID.randomUUID());
            }
            return existing;
        });
    }

    @Override
    public void onOpAppended(MyObjectOpRecord op) {
        for (Session s : sessionsByPlayer.values()) {
            if (!s.isActive()) continue;
            if (s.snapshotInProgress) continue;
            s.markDirty();
        }
    }

    public void handleHello(ServerPlayer player, SyncHelloPayload hello) {
        if (player == null) return;
        if (!SyncConstants.DATASET_ID.equals(hello.dataset())) return;

        Session s = getOrCreateSession(player);
        s.sessionId = UUID.randomUUID();
        s.snapshotInProgress = false;
        s.snapshotId = SnapshotPlan.NIL_UUID;
        s.snapshotBaseSeq = 0L;
        s.snapshotIter = null;
        s.snapshotChunkIndex = 0;
        s.lastAckSeq = hello.clientLastAppliedSeq();
        s.markDirty();

        long serverSeq = store.serverCurrentSeq();
        long minExclusive = store.serverMinAvailableSeqExclusive();

        boolean needSnapshot = (hello.clientLastAppliedSeq() < minExclusive) || (hello.clientLastAppliedSeq() > serverSeq);

        SnapshotPlan plan;
        if (needSnapshot) {
            UUID snapshotId = UUID.randomUUID();
            long baseSeq = serverSeq;
            plan = SnapshotPlan.required(snapshotId, baseSeq);

            s.snapshotInProgress = true;
            s.snapshotId = snapshotId;
            s.snapshotBaseSeq = baseSeq;
            s.snapshotChunkIndex = 0;

            // Snapshot is a consistent read view due to store read lock when we pull list below.
            List<ClueObject> snapshot = store.serverSnapshotAllObjectsSorted();
            s.snapshotIter = snapshot.iterator();
            s.snapshotTotal = snapshot.size();
        } else {
            plan = SnapshotPlan.none();
        }

        PacketDistributor.sendToPlayer(player, new SyncStartPayload(s.sessionId, serverSeq, minExclusive, plan));

        if (needSnapshot) {
            PacketDistributor.sendToPlayer(player, new SyncSnapshotBeginPayload(
                    s.sessionId, s.snapshotId, s.snapshotBaseSeq, s.snapshotTotal, SyncConstants.MAX_SNAPSHOT_OBJS_PER_CHUNK
            ));
        } else {
//            TODO: will this be ok...
//            PacketDistributor.sendToPlayer(player, new SyncRequestOpsPayload(s.sessionId, hello.clientLastAppliedSeq()));
        }
    }

    public void handleRequestOps(ServerPlayer player, SyncRequestOpsPayload req) {
        if (player == null) return;
        Session s = sessionsByPlayer.get(player.getUUID());
        if (s == null || !s.sessionId.equals(req.sessionId())) return;
        if (s.snapshotInProgress) return;

        s.lastAckSeq = Math.max(s.lastAckSeq, req.fromSeqExclusive());
        s.markDirty();
    }

    public void handleAck(ServerPlayer player, SyncAckPayload ack) {
        if (player == null) return;
        Session s = sessionsByPlayer.get(player.getUUID());
        if (s == null || !s.sessionId.equals(ack.sessionId())) return;

        s.lastAckSeq = Math.max(s.lastAckSeq, ack.lastAppliedSeq());
        s.markDirty();
    }

    public void tickPump() {
        store.serverPruneOplogIfNeeded();

        for (Session s : sessionsByPlayer.values()) {
            if (!s.isActive()) continue;
            if (!s.dirty) continue;
            s.dirty = false;

            if (s.snapshotInProgress) {
                pumpSnapshot(s);
                continue;
            }

            pumpOps(s);
        }
    }

    private void pumpSnapshot(Session s) {
        if (s.snapshotIter == null) {
            s.snapshotInProgress = false;
            s.markDirty();
            return;
        }

        int chunksSent = 0;
        while (chunksSent < SyncConstants.MAX_CHUNKS_PER_TICK_PER_PLAYER && s.snapshotIter.hasNext()) {
            List<ClueObject> chunk = new ArrayList<>(SyncConstants.MAX_SNAPSHOT_OBJS_PER_CHUNK);
            while (chunk.size() < SyncConstants.MAX_SNAPSHOT_OBJS_PER_CHUNK && s.snapshotIter.hasNext()) {
                chunk.add(s.snapshotIter.next());
            }

            boolean last = !s.snapshotIter.hasNext();
            PacketDistributor.sendToPlayer(s.player, new SyncSnapshotChunkPayload(
                    s.sessionId, s.snapshotId, s.snapshotChunkIndex++, last, chunk
            ));
            chunksSent++;

            if (last) {
                PacketDistributor.sendToPlayer(s.player, new SyncSnapshotEndPayload(s.sessionId, s.snapshotId, s.snapshotBaseSeq));
                s.snapshotInProgress = false;
                s.snapshotIter = null;
                s.lastAckSeq = s.snapshotBaseSeq; // client will ack; we can begin from baseSeq
                s.markDirty();
                return;
            }
        }

        if (s.snapshotInProgress) {
            s.markDirty(); // continue sending next tick
        }
    }

    private void pumpOps(Session s) {
        long serverSeq = store.serverCurrentSeq();
        if (s.lastAckSeq >= serverSeq) return;

        int batchesSent = 0;
        long from = s.lastAckSeq;

        while (batchesSent < SyncConstants.MAX_OP_BATCHES_PER_TICK_PER_PLAYER) {
            List<MyObjectOpRecord> ops = store.serverGetOpsAfter(from, SyncConstants.MAX_OPS_PER_BATCH);
            if (ops.isEmpty()) {
                // Oplog may have been pruned; force snapshot restart
                UUID snapshotId = UUID.randomUUID();
                long baseSeq = serverSeq;

                s.sessionId = UUID.randomUUID();
                s.snapshotInProgress = true;
                s.snapshotId = snapshotId;
                s.snapshotBaseSeq = baseSeq;
                s.snapshotChunkIndex = 0;

                List<ClueObject> snapshot = store.serverSnapshotAllObjectsSorted();
                s.snapshotIter = snapshot.iterator();
                s.snapshotTotal = snapshot.size();

                PacketDistributor.sendToPlayer(s.player, new SyncStartPayload(
                        s.sessionId, serverSeq, store.serverMinAvailableSeqExclusive(), SnapshotPlan.required(snapshotId, baseSeq)
                ));
                PacketDistributor.sendToPlayer(s.player, new SyncSnapshotBeginPayload(
                        s.sessionId, s.snapshotId, s.snapshotBaseSeq, s.snapshotTotal, SyncConstants.MAX_SNAPSHOT_OBJS_PER_CHUNK
                ));
                s.markDirty();
                return;
            }

            long to = ops.get(ops.size() - 1).seq();
            PacketDistributor.sendToPlayer(s.player, new SyncOpBatchPayload(s.sessionId, from, to, ops));
            from = to;
            batchesSent++;

            if (from >= serverSeq) break;
        }

        s.markDirty(); // keep pumping until caught up
    }

    public static final class Session {
        public final ServerPlayer player;
        public UUID sessionId;

        public boolean snapshotInProgress;
        public UUID snapshotId = SnapshotPlan.NIL_UUID;
        public long snapshotBaseSeq;
        public int snapshotChunkIndex;
        public int snapshotTotal;
        public Iterator<ClueObject> snapshotIter;

        public long lastAckSeq;
        public volatile boolean dirty = true;

        Session(ServerPlayer player, UUID sessionId) {
            this.player = player;
            this.sessionId = sessionId;
        }

        public boolean isActive() {
            return player != null && player.connection != null;
        }

        public void markDirty() {
            dirty = true;
        }
    }
}
