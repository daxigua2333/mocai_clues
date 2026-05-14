package io.github.daxigua2333.cmagic_clue.entry.client;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.guis.ClueBookScreen;
import io.github.daxigua2333.cmagic_clue.guis.widget.FixedTextureButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = CMagicClue.MODID, value = Dist.CLIENT)
public final class ScreenInitHook {
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        // Check if the screen is EITHER Survival Inventory OR Creative Inventory
        if (screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen) {

            // Cast to AbstractContainerScreen to access getGuiLeft() and getGuiTop()
            AbstractContainerScreen<?> gui = (AbstractContainerScreen<?>) screen;

            int left = gui.getGuiLeft();
            int top = gui.getGuiTop();
            // Calculate position
            int x = left - 36;
            int y = top;
            // Optional: If you want to tweak the position specifically for Creative mode
            // because of the tabs, you can add a check here:
            if (screen instanceof CreativeModeInventoryScreen) {
                // Creative GUI is often wider or has tabs on the left/top
                // x += 10; // example adjustment
            }
            event.addListener(
                    new FixedTextureButton(x, y, 32, 32, Component.empty(), btn -> {
                        Minecraft.getInstance().setScreen(new ClueBookScreen(Minecraft.getInstance().player));
                    }, ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "clue_book_open_button"))
            );
        }
    }
}
