package io.github.daxigua2333.cmagic_clue.blocks;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocksRegistry {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CMagicClue.MODID);
//    public static final DeferredBlock<Block> INVISIBLE_THIN_PLANE = BLOCKS.register(
//        "invisible_thin_plane",
//        () -> new InvisibleThinPlaneBlock(
//                BlockBehaviour.Properties.of()
//                        .noCollission()
//                        .noOcclusion()
//                        .strength(-1, 3e6f)  // bedrock
//                        .sound(SoundType.GLASS)
//        )
//    );

//    public static final DeferredItem<BlockItem> INVISIBLE_THIN_PLANE_ITEM = ModItemsRegistry.ITEMS.registerSimpleBlockItem("invisible_thin_plane", INVISIBLE_THIN_PLANE);

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }

}
