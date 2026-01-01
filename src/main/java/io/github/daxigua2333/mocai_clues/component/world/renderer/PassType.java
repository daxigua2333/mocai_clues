package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BasePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BlockOutlinePass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** registry for render pass */
public enum PassType {
    BLOCK_OUTLINE,
//        FLASH_POINT,
    ;

    public static final Codec<PassType> CODEC = Codec.STRING.xmap(PassType::valueOf, Enum::toString);

    @OnlyIn(value = Dist.CLIENT)
    public static BasePass getPass(PassType type) {
        return switch (type) {
            case BLOCK_OUTLINE -> new BlockOutlinePass();
        };
    }

    // just use singletons
//    @OnlyIn(value = Dist.CLIENT)
//    private static final BlockOutlinePass boPass = new BlockOutlinePass();
}

