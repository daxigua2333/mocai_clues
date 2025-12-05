package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;

public enum ClueType {
    // TODO
    MANUAL,
    FOOTPRINT,
    FOOTPRINT1,
    FOOTPRINT2,
    FOOTPRINT3,
    FOOTPRINT4,
    FOOTPRINT5,
    FOOTPRINT6,
    FOOTPRINT7,
    FOOTPRINT8,
    ;

    public static final Codec<ClueType> CODEC =
            Codec.STRING.xmap(ClueType::valueOf, Enum::name);
}
