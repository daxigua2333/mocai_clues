package io.github.daxigua2333.mocai_clues.footprints.statics;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.footprints.Events.BlockBecameAirEvent;
import io.github.daxigua2333.mocai_clues.footprints.Events.BlockBecameNonAirEvent;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class DeleteEventHooks {

    @SubscribeEvent
    public static void onBecameAir(BlockBecameAirEvent event) {
        FootprintServerHelper.deleteByBlockPos(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onBecameNonAir(BlockBecameNonAirEvent event) {
        // TODO: .......... 我处理不好.............
        if (event.getNew().is(Blocks.WATER)) {
            FootprintServerHelper.deleteByBlockPos(event.getLevel(), event.getPos().below());
        }
    }
}
