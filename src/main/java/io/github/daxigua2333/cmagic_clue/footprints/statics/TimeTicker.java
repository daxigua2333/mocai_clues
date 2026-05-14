package io.github.daxigua2333.cmagic_clue.footprints.statics;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.footprints.data.TimestampSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = CMagicClue.MODID)
public class TimeTicker {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!HookToggle.isEnabled()) {return;}

//        level.getProfiler().push("mocai_clues:time+part");
        TimestampSavedData.getInstance(serverLevel).tick(serverLevel);
//        level.getProfiler().pop();
    }
}
