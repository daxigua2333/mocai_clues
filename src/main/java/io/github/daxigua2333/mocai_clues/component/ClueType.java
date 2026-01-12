package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;

public enum ClueType {
    // TODO
    MANUAL,
//    CLUE_BOOK,
    FOOTPRINT,
    ;

    public static final Codec<ClueType> CODEC =
            Codec.STRING.xmap(ClueType::valueOf, Enum::name);
}
