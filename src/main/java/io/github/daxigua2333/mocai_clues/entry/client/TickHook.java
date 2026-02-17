//package io.github.daxigua2333.mocai_clues.entry.client;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.component.ComponentType;
//import io.github.daxigua2333.mocai_clues.component.world.finder.FlashDotSet;
//import io.github.daxigua2333.mocai_clues.data.ObjectsWithLocation;
//import io.github.daxigua2333.mocai_clues.data.common.DataManager;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.api.distmarker.OnlyIn;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.ClientTickEvent;
//
//import java.util.List;
//
//@OnlyIn(Dist.CLIENT)
//@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
//public final class TickHook {
//    private static final int CHUNK_RADIUS = 5;
//
//    @SubscribeEvent
//    public static void onClientTick(ClientTickEvent.Post event) {
//        Minecraft mc = Minecraft.getInstance();
//        ClientLevel level = mc.level;
//        if (level == null || mc.player == null || mc.isPaused()) return;
//        // don’t spawn every tick; every 4 ticks is plenty for a “flash”
//        if ((level.getGameTime() & 3) != 0) return;
//
//        List<ObjectsWithLocation> data = DataManager.Client.retrieveByNearbyLoadedChunks(level, mc.player.chunkPosition(), Math.min(CHUNK_RADIUS, Minecraft.getInstance().options.renderDistance().get()));
//        for (ObjectsWithLocation ol : data) {
//            for (ClueObject obj : ol.objects()) {
//                FlashDotSet compo = obj.getComponent(ComponentType.FLASH_DOT_SET);
//                if (compo == null) continue;
//                compo.spawn(level);
//            }
//        }
//    }
//}
