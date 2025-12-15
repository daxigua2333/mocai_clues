package io.github.daxigua2333.mocai_clues.data.sync;


import io.github.daxigua2333.mocai_clues.data.sync.client.MyObjectSyncClientPayloadHandler;
import io.github.daxigua2333.mocai_clues.data.sync.payload.*;
import io.github.daxigua2333.mocai_clues.data.sync.server.MyObjectSyncServerPayloadHandler;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = SyncConstants.MODID)
public final class MyObjectSyncNetworking {
    private MyObjectSyncNetworking() {}

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SyncConstants.PROTOCOL_VERSION)
                .executesOn(HandlerThread.NETWORK);

        registrar.commonToServer(SyncHelloPayload.TYPE, SyncHelloPayload.STREAM_CODEC, MyObjectSyncServerPayloadHandler::handleHello);
        registrar.commonToServer(SyncRequestOpsPayload.TYPE, SyncRequestOpsPayload.STREAM_CODEC, MyObjectSyncServerPayloadHandler::handleRequestOps);
        registrar.commonToServer(SyncAckPayload.TYPE, SyncAckPayload.STREAM_CODEC, MyObjectSyncServerPayloadHandler::handleAck);

        registrar.commonToClient(SyncStartPayload.TYPE, SyncStartPayload.STREAM_CODEC, MyObjectSyncClientPayloadHandler::handleStart);
        registrar.commonToClient(SyncSnapshotBeginPayload.TYPE, SyncSnapshotBeginPayload.STREAM_CODEC, MyObjectSyncClientPayloadHandler::handleSnapshotBegin);
        registrar.commonToClient(SyncSnapshotChunkPayload.TYPE, SyncSnapshotChunkPayload.STREAM_CODEC, MyObjectSyncClientPayloadHandler::handleSnapshotChunk);
        registrar.commonToClient(SyncSnapshotEndPayload.TYPE, SyncSnapshotEndPayload.STREAM_CODEC, MyObjectSyncClientPayloadHandler::handleSnapshotEnd);
        registrar.commonToClient(SyncOpBatchPayload.TYPE, SyncOpBatchPayload.STREAM_CODEC, MyObjectSyncClientPayloadHandler::handleOpBatch);
        registrar.commonToClient(SyncErrorPayload.TYPE, SyncErrorPayload.STREAM_CODEC, MyObjectSyncClientPayloadHandler::handleError);
    }

}
