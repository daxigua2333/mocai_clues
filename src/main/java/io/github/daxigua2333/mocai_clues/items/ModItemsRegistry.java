package io.github.daxigua2333.mocai_clues.items;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItemsRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MoCaiClues.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MoCaiClues.MODID);

    public static final Supplier<Item> CLUE_WAND_ITEM = ITEMS.register("clue_wand",
        () -> new ClueWandItem(new Item.Properties().stacksTo(1))
    );
    public static final Supplier<Item> CLUE_FINDER_ITEM = ITEMS.register("clue_finder",
        () -> new ClueFinderItem(new Item.Properties().stacksTo(1))
    );


    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("clue_items", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.mocai_clues")) //The language key for the title of your CreativeModeTab
        .withTabsBefore(CreativeModeTabs.COMBAT)
        .icon(() -> CLUE_WAND_ITEM.get().getDefaultInstance())
        .displayItems((parameters, output) -> {
            output.accept(CLUE_WAND_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            output.accept(CLUE_FINDER_ITEM.get());
        }).build());


    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }
}
