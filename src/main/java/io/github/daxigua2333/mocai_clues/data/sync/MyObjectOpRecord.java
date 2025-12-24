package io.github.daxigua2333.mocai_clues.data.sync;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record MyObjectOpRecord(
        long seq,
        byte opType,
        String key,
        Optional<ClueObject> value
) {
    public static final StreamCodec<ByteBuf, MyObjectOpRecord> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, MyObjectOpRecord::seq,
                    ByteBufCodecs.BYTE, MyObjectOpRecord::opType,
                    ByteBufCodecs.STRING_UTF8, MyObjectOpRecord::key,
                    ByteBufCodecs.optional(ClueObject.STREAM_CODEC), MyObjectOpRecord::value,
                    MyObjectOpRecord::new
            );

    public MyObjectOpType type() {
        return MyObjectOpType.fromId(opType);
    }

    public static MyObjectOpRecord upsert(long seq, String key, ClueObject value) {
        return new MyObjectOpRecord(seq, MyObjectOpType.UPSERT.id, key, Optional.of(value));
    }

    public static MyObjectOpRecord delete(long seq, String key) {
        return new MyObjectOpRecord(seq, MyObjectOpType.DELETE.id, key, Optional.empty());
    }
}
