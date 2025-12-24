package io.github.daxigua2333.mocai_clues.data.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderClientSyncedEvent;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderDataChangeEvent;
import io.github.daxigua2333.mocai_clues.data.client.ClientObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid= MoCaiClues.MODID)
public class ClueObjectHolderNetworkStatics {


    // ======= register =======
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN);

        registrar.playToClient(
            ClueObjectHolderFullPayload.TYPE,
            ClueObjectHolderFullPayload.STREAM_CODEC,
            (payload, ctx) -> {
                ctx.enqueueWork(() -> {
                    ClientObjectHolderInSavedData.getInstance().replaceAll(payload.data());
                });
            }
        );

        registrar.playToClient(
            ClueObjectHolderDeltaPayload.TYPE,
            ClueObjectHolderDeltaPayload.STREAM_CODEC,
            (payload, ctx) -> {
                ctx.enqueueWork(() -> {
                    ClientObjectHolderInSavedData.getInstance().applyDelta(payload.deltaPayload());
                });
            }
        );
    }

//        seems to only work on multi...
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) {return;}
        ServerPlayer player = (ServerPlayer) event.getEntity();
        ServerLevel level = player.serverLevel();
        var SD = ClueObjectHolderInSavedData.getInstance(level.getServer());
        // TODO: test
//        SD.clear();
        SD.syncAllTo(player);
    }

}
