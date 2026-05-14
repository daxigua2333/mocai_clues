package io.github.daxigua2333.cmagic_clue.component.system.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.cmagic_clue.component.data.renderer.BaseRendererData;
import io.github.daxigua2333.cmagic_clue.component.data.renderer.BlockOutlineData;
import io.github.daxigua2333.cmagic_clue.component.system.renderer.pass.BasePass;
import io.github.daxigua2333.cmagic_clue.component.system.renderer.pass.BlockOutlinePass;
import io.github.daxigua2333.cmagic_clue.utils.EnumCodecProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * registry for render pass
 */
public enum PassType {
    BLOCK_OUTLINE,
//        FLASH_POINT,
    ;

    public static final Codec<PassType> CODEC = EnumCodecProvider.createCodec(PassType.class);

    @OnlyIn(Dist.CLIENT)
    private static class ClientLookup {
        public static final BlockOutlinePass BLOCK_OUTLINE_PASS = new BlockOutlinePass();
    }

    @OnlyIn(value = Dist.CLIENT)
    public BasePass getPass() {
        return switch (this) {
            case BLOCK_OUTLINE -> ClientLookup.BLOCK_OUTLINE_PASS;
        };
    }


    public MapCodec<? extends BaseRendererData> codec() {
        return switch (this) {
            case BLOCK_OUTLINE -> BlockOutlineData.CODEC;
        };
    }
}

