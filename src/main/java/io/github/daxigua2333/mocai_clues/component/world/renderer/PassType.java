package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.mocai_clues.component.world.renderer.data.BaseRendererData;
import io.github.daxigua2333.mocai_clues.component.world.renderer.data.BlockOutlineData;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BasePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BlockOutlinePass;
import io.github.daxigua2333.mocai_clues.utils.EnumCodecProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** registry for render pass */
public enum PassType {
    BLOCK_OUTLINE(new BlockOutlinePass()),
//        FLASH_POINT,
    ;

    private final BasePass PASS;
    public static final Codec<PassType> CODEC = EnumCodecProvider.createCodec(PassType.class);

    PassType(BasePass pass) {
        PASS = pass;
    }

    @OnlyIn(value = Dist.CLIENT)
    public BasePass getPass() {
        return PASS;
    }

    // TODO: just use singletons
//    @OnlyIn(value = Dist.CLIENT)
//    private static final BlockOutlinePass boPass = new BlockOutlinePass();

    public MapCodec<? extends BaseRendererData> codec() {
        return switch (this) {
            case BLOCK_OUTLINE -> BlockOutlineData.CODEC;
        };
    }
}

