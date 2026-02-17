package io.github.daxigua2333.mocai_clues.data;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

@Deprecated
public record ObjectHolderLocation<T>(Type type, T data) {
    public enum Type {
        SD, CHUNK, ENTITY;

        private static final Type[] VALUES = values();
        public static Type byId(int id) {
            if (id < 0 || id >= VALUES.length)
                throw new DecoderException("Bad ObjectHolderLocation.Type id: " + id);
            return VALUES[id];
        }
        // StreamCodec<ByteBuf, MyMode> works fine with RegistryFriendlyByteBuf too
        public static final StreamCodec<ByteBuf, Type> STREAM_CODEC =
                net.minecraft.network.codec.ByteBufCodecs.VAR_INT.map(
                        Type::byId,      // decode: int -> enum
                        Type::ordinal    // encode: enum -> int
                );
    }

    public record BlockPosWithFace(BlockPos pos, Direction face) {}


//    public static <T> StreamCodec<ByteBuf, ObjectHolderLocation<T>> streamCodec(StreamCodec<ByteBuf, T> dataStreamCodec) {
//        return StreamCodec.of(
//                (buf, inst) -> {
//                    Type.STREAM_CODEC.encode(buf, inst.type);
//                    dataStreamCodec.encode(buf, inst.data);
//                },
//                buf -> {
//                    return new ObjectHolderLocation<>(Type.STREAM_CODEC.decode(buf), dataStreamCodec.decode(buf));
//                }
//        );
//    }
}
