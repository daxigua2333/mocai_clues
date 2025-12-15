package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;

public enum ComponentType {
    // TODO
    // data
    DETAIL_DATA,
    ;

    public static final Codec<ComponentType> CODEC =
            Codec.STRING.xmap(ComponentType::valueOf, Enum::name);

}
