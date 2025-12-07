package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;

public enum ComponentType {
    // TODO
    // data
    DETAIL_DATA,
    // network
    MANUAL_CLUE_SERVER_HANDLER,
    // storage
    SAVED_DATA_HOLDER
    ;

    public static final Codec<ComponentType> CODEC =
            Codec.STRING.xmap(ComponentType::valueOf, Enum::name);

}
