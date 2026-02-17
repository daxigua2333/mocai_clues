//package io.github.daxigua2333.mocai_clues.entry.common;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.github.daxigua2333.mocai_clues.component.system.discovery.InteractEvent;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.entity.player.Player;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.event.tick.PlayerTickEvent;
//
//@EventBusSubscriber(modid = MoCaiClues.MODID)
//public final class PlayerTickHook {
//    @SubscribeEvent
//    public static void onPlayerTick(PlayerTickEvent.Post event) {
//        Player player = event.getEntity();
//
//        // handle feet block pos
//        InteractEvent.walkOn(player, player.getOnPos());
//
//    }
//
//}
