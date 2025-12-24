package io.github.daxigua2333.mocai_clues.entry.client;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public final class RenderPipelineHook {
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push("mocai_clues:render");

        if (!Config.CLIENT.FOOTPRINT_DO_RENDER.getAsBoolean()) {
            return;
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
//            renderFootprints(event);  // TODO: event.getPartialTick()
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
//            BlockOutlineRenderer.render();
        }

        mc.getProfiler().pop();
    }
}
