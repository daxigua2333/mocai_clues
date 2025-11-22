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

    public static final Supplier<Item> CLUE_WAND_ITEM = ITEMS.register("clue_wand",
        () -> new ClueWandItem(new Item.Properties().stacksTo(1))
    );
    public static final Supplier<Item> CLUE_FINDER_ITEM = ITEMS.register("clue_finder",
        () -> new ClueFinderItem(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
