//package io.github.daxigua2333.mocai_clues.data.networks.snapshot;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
//import net.neoforged.neoforge.network.registration.HandlerThread;
//import net.neoforged.neoforge.network.registration.PayloadRegistrar;
//
//@EventBusSubscriber(modid = MoCaiClues.MODID)
//public final class DbSnapshotNetworking {
//    private DbSnapshotNetworking() {}
//
//    @SubscribeEvent
//    public static void register(RegisterPayloadHandlersEvent event) {
//        PayloadRegistrar registrar = event.registrar("1").executesOn(HandlerThread.NETWORK);
//
//        registrar.playToClient(
//            DbSnapshotPayloads.DbSnapshotStartPayload.TYPE,
//            DbSnapshotPayloads.DbSnapshotStartPayload.STREAM_CODEC,
//            DbSnapshotReceiver::handleStart
//        );
//
//        registrar.playToClient(
//            DbSnapshotPayloads.DbSnapshotChunkPayload.TYPE,
//            DbSnapshotPayloads.DbSnapshotChunkPayload.STREAM_CODEC,
//            DbSnapshotReceiver::handleChunk
//        );
//
//        registrar.playToServer(
//            DbSnapshotPayloads.DbSnapshotAckPayload.TYPE,
//            DbSnapshotPayloads.DbSnapshotAckPayload.STREAM_CODEC,
//            DbSnapshotServerAckHandler::handleAck
//        );
//    }
//}
