package io.github.daxigua2333.mocai_clues.data.sync.client;


import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.payload.*;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MyObjectSyncClientPayloadHandler {
    private MyObjectSyncClientPayloadHandler() {}

    public static void handleStart(final SyncStartPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isClientInitialized()) return;
        MyObjectSync.client().handleStart(payload);
    }

    public static void handleSnapshotBegin(final SyncSnapshotBeginPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isClientInitialized()) return;
        MyObjectSync.client().handleSnapshotBegin(payload);
    }

    public static void handleSnapshotChunk(final SyncSnapshotChunkPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isClientInitialized()) return;
        MyObjectSync.client().handleSnapshotChunk(payload);
    }

    public static void handleSnapshotEnd(final SyncSnapshotEndPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isClientInitialized()) return;
        MyObjectSync.client().handleSnapshotEnd(payload);
    }

    public static void handleOpBatch(final SyncOpBatchPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isClientInitialized()) return;
        MyObjectSync.client().handleOpBatch(payload);
    }

    public static void handleError(final SyncErrorPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isClientInitialized()) return;
        MyObjectSync.client().handleError(payload);
    }
}
