package io.github.daxigua2333.mocai_clues.component.world.renderer.data;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BasePass;

public abstract class BaseRendererData {
    public abstract PassType getPassType();

    public static final Codec<BaseRendererData> CODEC = PassType.CODEC.dispatch(
            "type",
            BaseRendererData::getPassType,
            PassType::codec
    );

}
