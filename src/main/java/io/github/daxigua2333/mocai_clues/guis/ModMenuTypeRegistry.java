package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.guis.whitelist.WhitelistMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypeRegistry {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MoCaiClues.MODID);
    public static final Supplier<MenuType<ClueInventoryMenu>> CLUE_INVENTORY_MENU =
            MENU_TYPES.register("clue_inventory_menu", () ->
                    new MenuType<>(ClueInventoryMenu::new, FeatureFlags.DEFAULT_FLAGS
                    ));

//    public static final Supplier<MenuType<WhitelistMenu>> WHITELIST_MENU =
//            MENU_TYPES.register("whitelist_menu", () -> IMenuTypeExtension.create(WhitelistMenu::new));

    public static final Supplier<MenuType<WhitelistMenu>> WHITELIST_MENU =
            MENU_TYPES.register("whitelist_menu", () -> new MenuType<>(WhitelistMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus bus) {
        MENU_TYPES.register(bus);
    }

}
