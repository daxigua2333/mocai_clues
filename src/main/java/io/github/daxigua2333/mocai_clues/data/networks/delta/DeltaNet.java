//package io.github.daxigua2333.mocai_clues.data.networks.delta;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.fml.common.Mod;
//import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
//import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
//import net.neoforged.neoforge.network.registration.PayloadRegistrar;
//
//@EventBusSubscriber(modid = MoCaiClues.MODID)
//public final class DeltaNet {
//    private DeltaNet() {}
//
//    @SubscribeEvent
//    public static void register(final RegisterPayloadHandlersEvent event) {
//        final PayloadRegistrar registrar = event.registrar("1");
//
//        registrar.playToClient(
//                DeltaBatchPayload.TYPE,
//                DeltaBatchPayload.STREAM_CODEC,
//                DeltaSyncClient::handleDeltaBatch
//        );
//
//        registrar.playToClient(
//                ResyncRequiredPayload.TYPE,
//                ResyncRequiredPayload.STREAM_CODEC,
//                DeltaSyncClient::handleResyncRequired
//        );
//
//        registrar.playToServer(
//                DeltaRequestPayload.TYPE,
//                DeltaRequestPayload.STREAM_CODEC,
//                DeltaSyncServer::handleDeltaRequest
//        );
//    }
//}
