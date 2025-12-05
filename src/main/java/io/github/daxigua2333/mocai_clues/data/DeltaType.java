package io.github.daxigua2333.mocai_clues.data;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum DeltaType {
    PUT, REMOVE, CLEAR;

    public static final Codec<DeltaType> CODEC =
            Codec.STRING.xmap(DeltaType::valueOf, Enum::name);
    public static final StreamCodec<RegistryFriendlyByteBuf, DeltaType> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(DeltaType.CODEC);
}
