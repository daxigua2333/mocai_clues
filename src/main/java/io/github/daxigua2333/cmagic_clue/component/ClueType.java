package io.github.daxigua2333.cmagic_clue.component;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.cmagic_clue.utils.EnumCodecProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum ClueType {
    MANUAL,
    ITEM,
    CLUE_BOOK,
    FOOTPRINT,
    ;

    public static final Codec<ClueType> CODEC = EnumCodecProvider.createCodec(ClueType.class);
    public static final StreamCodec<ByteBuf, ClueType> STREAM_CODEC = EnumCodecProvider.createStreamCodec(ClueType.class);
}
