package io.github.daxigua2333.cmagic_clue.data_attachments.statics;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber
public class BlockBrokenDropsCluesListener {
    @SubscribeEvent
    private static void OnBlockBroken(BlockEvent.BreakEvent event) {
        ClueContainerAttachmentHelper.remove(event.getPlayer().level(), event.getPos());
    }
}
