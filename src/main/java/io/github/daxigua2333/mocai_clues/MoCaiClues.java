package io.github.daxigua2333.mocai_clues;

import io.github.daxigua2333.mocai_clues.blocks.ModBlocksRegistry;
import io.github.daxigua2333.mocai_clues.data_attachments.ModDataAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.footprints.ModFootprintRegistry;
import io.github.daxigua2333.mocai_clues.guis.ClueInventoryMenu;
import io.github.daxigua2333.mocai_clues.guis.ModMenuTypeRegistry;
import io.github.daxigua2333.mocai_clues.items.ClueFinderItem;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.*;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(MoCaiClues.MODID)
public class MoCaiClues {
    public static final String MODID = "mocai_clues";
    public static final Logger LOGGER = LogUtils.getLogger();


//    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
//    // Creates a new Block with the id "mocai_clues:example_block", combining the namespace and path
//    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
//    // Creates a new BlockItem with the id "mocai_clues:example_block", combining the namespace and path
//    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);


    public MoCaiClues(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus
        ModBlocksRegistry.register(modEventBus);
        ModItemsRegistry.register(modEventBus);
        ModCreativeTabRegistry.register(modEventBus);
        ModDataComponentsRegistry.register(modEventBus);
        ModDataAttachmentRegistry.register(modEventBus);
        ModMenuTypeRegistry.register(modEventBus);
        ModFootprintRegistry.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (MoCaiClues) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

//        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
//            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
//        }
//
//        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());
//
//        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
//            event.accept(EXAMPLE_BLOCK_ITEM);
//        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

}
