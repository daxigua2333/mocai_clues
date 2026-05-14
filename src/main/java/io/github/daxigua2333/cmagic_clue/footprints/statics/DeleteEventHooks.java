package io.github.daxigua2333.cmagic_clue.footprints.statics;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.footprints.Events.BlockBecameAirEvent;
import io.github.daxigua2333.cmagic_clue.footprints.Events.BlockBecameNonAirEvent;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = CMagicClue.MODID)
public final class DeleteEventHooks {

    // TODO: cannot run on dedicate server when creating a new world
    @SubscribeEvent
    public static void onBecameAir(BlockBecameAirEvent event) {
        if (!HookToggle.isEnabled()) {return;}
//        FootprintServerHelper.deleteByBlockPos(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public static void onBecameNonAir(BlockBecameNonAirEvent event) {
        if (!HookToggle.isEnabled()) {return;}
        // TODO: .......... 我处理不好.............
        if (event.getNew().is(Blocks.WATER)) {
//            FootprintServerHelper.deleteByBlockPos(event.getLevel(), event.getPos().below());
        }
    }
}
