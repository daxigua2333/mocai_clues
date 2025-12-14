package io.github.daxigua2333.mocai_clues.data.sync.client;

import io.github.daxigua2333.mocai_clues.data.sync.NitriteMyObjectStore;
import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.github.daxigua2333.mocai_clues.data.sync.payload.*;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class MyObjectSyncClient {

    private final NitriteMyObjectStore store;

    private volatile UUID sessionId = SnapshotPlan.NIL_UUID;

    private volatile boolean snapshotActive = false;
    private volatile UUID snapshotId = SnapshotPlan.NIL_UUID;
    private volatile long snapshotBaseSeq = 0L;

    private final AtomicInteger resyncAttempts = new AtomicInteger(0);

    public MyObjectSyncClient(NitriteMyObjectStore store) {
        this.store = store;
    }

    public NitriteMyObjectStore store() {
        return store;
    }

    public void startHello() {
        long last = store.clientLastAppliedSeq();
        PacketDistributor.sendToServer(new SyncHelloPayload(SyncConstants.DATASET_ID, last, 1));
    }

    public void handleStart(SyncStartPayload start) {
        this.sessionId = start.sessionId();

        if (start.snapshotPlan().required()) {
            snapshotActive = true;
            snapshotId = start.snapshotPlan().snapshotId();
            snapshotBaseSeq = start.snapshotPlan().baseSeq();
            store.clientApplySnapshotClearAndBegin(snapshotBaseSeq);
            resyncAttempts.set(0);
            return;
        }

        snapshotActive = false;
        snapshotId = SnapshotPlan.NIL_UUID;
        snapshotBaseSeq = 0L;

        long last = store.clientLastAppliedSeq();
        PacketDistributor.sendToServer(new SyncRequestOpsPayload(sessionId, last));
        resyncAttempts.set(0);
    }

    public void handleSnapshotBegin(SyncSnapshotBeginPayload begin) {
        if (!begin.sessionId().equals(sessionId)) return;
        if (!snapshotActive) return;
        if (!begin.snapshotId().equals(snapshotId)) return;
    }

    public void handleSnapshotChunk(SyncSnapshotChunkPayload chunk) {
        if (!chunk.sessionId().equals(sessionId)) return;
        if (!snapshotActive) return;
        if (!chunk.snapshotId().equals(snapshotId)) return;

        if (chunk.objects().size() > SyncConstants.MAX_SNAPSHOT_OBJS_PER_CHUNK * 4) {
            requestFullResync("snapshot_chunk_too_large");
            return;
        }

        for (var obj : chunk.objects()) {
            store.clientApplySnapshotUpsert(obj, null);
        }
    }

    public void handleSnapshotEnd(SyncSnapshotEndPayload end) {
        if (!end.sessionId().equals(sessionId)) return;
        if (!snapshotActive) return;
        if (!end.snapshotId().equals(snapshotId)) return;

        snapshotActive = false;

        PacketDistributor.sendToServer(new SyncAckPayload(sessionId, end.baseSeq()));
        PacketDistributor.sendToServer(new SyncRequestOpsPayload(sessionId, end.baseSeq()));
    }

    public void handleOpBatch(SyncOpBatchPayload batch) {
        if (!batch.sessionId().equals(sessionId)) return;
        if (snapshotActive) return;

        if (batch.ops().size() > SyncConstants.MAX_OPS_PER_BATCH * 4) {
            requestFullResync("op_batch_too_large");
            return;
        }

        try {
            for (var op : batch.ops()) {
                store.clientApplyRemoteOp(op);
            }
            PacketDistributor.sendToServer(new SyncAckPayload(sessionId, store.clientLastAppliedSeq()));
        } catch (IllegalStateException gap) {
            PacketDistributor.sendToServer(new SyncRequestOpsPayload(sessionId, store.clientLastAppliedSeq()));
            if (resyncAttempts.incrementAndGet() >= 3) {
                requestFullResync("gap_persist");
            }
        } catch (Exception e) {
            requestFullResync("apply_failed");
        }
    }

    public void handleError(SyncErrorPayload err) {
        if (!err.sessionId().equals(sessionId)) return;
        requestFullResync("server_error_" + err.code());
    }

    private void requestFullResync(String reason) {
        snapshotActive = false;
        snapshotId = SnapshotPlan.NIL_UUID;
        snapshotBaseSeq = 0L;
        resyncAttempts.incrementAndGet();
        startHello();
    }
}
