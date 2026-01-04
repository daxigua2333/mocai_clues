package io.github.daxigua2333.mocai_clues.component.world.renderer.data;

import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;

public class BlockOutlineData extends BaseRendererData{
    @Override
    public PassType getPassType() {
        return PassType.BLOCK_OUTLINE;
    }

    public static final MapCodec<BlockOutlineData> CODEC = MapCodec.unit(new BlockOutlineData());
}
