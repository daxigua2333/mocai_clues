package io.github.daxigua2333.mocai_clues.entry.client;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.guis.ClueBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public final class ScreenInitHook {
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen inv)) return;
        // Top-left corner of the INVENTORY GUI background:
        int left = inv.getGuiLeft();
        int top  = inv.getGuiTop();
        // Choose a position. These coordinates are relative to the inventory GUI.
        // If you put it at left+2/top+2 it may overlap armor slots, so many mods place it just outside:
        int x = left - 20;   // just to the left of the GUI
        int y = top + 4;     // near the top
        x = 4;
        y = 4;
//        inv.addRenderableWidget(
        event.addListener(
            Button.builder(Component.literal("My"), btn -> {
                Minecraft.getInstance().setScreen(new ClueBookScreen());
            })
            .pos(x, y)
            .size(20, 20)
            .build()
        );
    }
}
