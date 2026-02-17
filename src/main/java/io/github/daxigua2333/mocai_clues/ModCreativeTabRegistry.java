package io.github.daxigua2333.mocai_clues;

import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoCaiClues.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CLUE_ITEMS = CREATIVE_MODE_TABS.register("clue_items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mocai_clues")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ModItemsRegistry.CLUE_WAND_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItemsRegistry.CLUE_WAND_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
                output.accept(ModItemsRegistry.CLUE_FINDER_ITEM.get());
//            output.accept(ModBlocksRegistry.INVISIBLE_THIN_PLANE_ITEM.get());
            }).build());

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }

}
