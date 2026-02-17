package io.github.daxigua2333.mocai_clues.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public interface EnumCodecProvider {

    /**
     * Creates a Codec based on Enum ordinal (int).
     */
    static <E extends Enum<E>> Codec<E> createCodec(Class<E> enumClass) {
        E[] values = enumClass.getEnumConstants();
        // Determine fallback (usually the first element)
        E fallback = values[0];

        return Codec.INT.comapFlatMap(
                id -> {
                    if (id >= 0 && id < values.length) {
                        return DataResult.success(values[id]);
                    }
                    // Option A: Return default/fallback
                    return DataResult.success(fallback);
                    // Option B: Error out
                    // return DataResult.error(() -> "Unknown enum id: " + id);
                },
                Enum::ordinal
        );
    }

    /**
     * Creates a StreamCodec (for Networking) based on Enum ordinal.
     */
    static <E extends Enum<E>> StreamCodec<ByteBuf, E> createStreamCodec(Class<E> enumClass) {
        E[] values = enumClass.getEnumConstants();

        return new StreamCodec<>() {
            @Override
            public E decode(ByteBuf buf) {
                int id = ByteBufCodecs.VAR_INT.decode(buf);
                if (id >= 0 && id < values.length) {
                    return values[id];
                }
                return values[0]; // Default fallback
            }

            @Override
            public void encode(ByteBuf buf, E value) {
                ByteBufCodecs.VAR_INT.encode(buf, value.ordinal());
            }
        };
    }
}
