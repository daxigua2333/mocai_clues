package io.github.daxigua2333.mocai_clues.data.sync.client;


import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = SyncConstants.MODID, value = Dist.CLIENT)
public final class MyObjectSyncClientEvents {
    private MyObjectSyncClientEvents() {}

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        if (!MyObjectSync.isClientInitialized()) return;
        MyObjectSync.client().startHello();
    }
}
