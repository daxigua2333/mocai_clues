package io.github.daxigua2333.cmagic_clue.guis;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.guis.whitelist.SingleSlotWhitelistScreen;
import io.github.daxigua2333.cmagic_clue.guis.whitelist.WhitelistMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;


@EventBusSubscriber(modid = CMagicClue.MODID, value = Dist.CLIENT)
public class ModScreenRegistry {
    // screen register
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypeRegistry.CLUE_INVENTORY_MENU.get(), ClueInventoryScreen::new);

        event.register(ModMenuTypeRegistry.WHITELIST_MENU.get(), new MenuScreens.ScreenConstructor<WhitelistMenu, SingleSlotWhitelistScreen>() {
            @Override
            public SingleSlotWhitelistScreen create(WhitelistMenu menu, Inventory inventory, Component component) {
                return new SingleSlotWhitelistScreen(menu, inventory, component);
            }

            @Override
            public void fromPacket(Component title, MenuType<WhitelistMenu> type, Minecraft mc, int windowId) {
                WhitelistMenu menu = type.create(windowId, mc.player.getInventory());
               SingleSlotWhitelistScreen screen = this.create(menu, mc.player.getInventory(), title);

                mc.player.containerMenu = menu;

                // 3. YOUR CUSTOM LOGIC HERE
                // Instead of mc.setScreen(screen), use your layer logic.
                // Assuming you have a method for this, or using a library that adds it:
                mc.pushGuiLayer(screen);

                // If you need to initialize the screen (usually done by setScreen)
//                screen.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
//                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)); // Optional: Click sound
            }
        });
    }
}
