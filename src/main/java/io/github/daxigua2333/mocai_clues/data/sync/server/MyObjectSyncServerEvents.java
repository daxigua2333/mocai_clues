package io.github.daxigua2333.mocai_clues.data.sync.server;

import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = SyncConstants.MODID)
public final class MyObjectSyncServerEvents {
    private MyObjectSyncServerEvents() {}

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!MyObjectSync.isServerInitialized()) return;
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            MyObjectSync.server().onPlayerLogout(sp);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!MyObjectSync.isServerInitialized()) return;
        MyObjectSync.server().tickPump();
    }
}
