package io.github.daxigua2333.mocai_clues.blocks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocksRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MoCaiClues.MODID);
    public static final DeferredBlock<Block> INVISIBLE_THIN_PLANE = BLOCKS.register(
        "invisible_thin_plane",
        () -> new InvisibleThinPlaneBlock(
                BlockBehaviour.Properties.of()
                        .noCollission()
                        .noOcclusion()
                        .strength(-1, 3e6f)  // bedrock
                        .sound(SoundType.GLASS)
        )
    );

    public static final DeferredItem<BlockItem> INVISIBLE_THIN_PLANE_ITEM = ModItemsRegistry.ITEMS.registerSimpleBlockItem("invisible_thin_plane", INVISIBLE_THIN_PLANE);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

}
