package io.github.daxigua2333.mocai_clues.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

@Deprecated
public record HolderLocation(Type type,
                             ChunkPos chunkPos, Direction face,
                             UUID entityId) {
    public enum Type {
        SD, CHUNK, ENTITY;
    }

    public HolderLocation(ChunkPos pos, Direction face) {
        this(Type.CHUNK, pos, face, null);
    }
    public HolderLocation(UUID entityId) {
        this(Type.ENTITY, null, null, entityId);
    }
    public HolderLocation() {
        this(Type.SD, null, null, null);
    }

    public static final StreamCodec<ByteBuf, HolderLocation> STREAM_CODEC =
            StreamCodec.of(
                    (buf, inst) -> {
                        switch (inst.type) {
                            case SD -> {
                                buf.writeInt(0);
                            }
                            case CHUNK -> {
                                buf.writeInt(1);
                                buf.writeLong(inst.chunkPos.toLong());
                                Direction.STREAM_CODEC.encode(buf, inst.face);
                            }
                            case ENTITY -> {
                                buf.writeInt(2);
                                UUIDUtil.STREAM_CODEC.encode(buf, inst.entityId);
                            }
                        }
                    },
                    buf -> {
                        int typeInt = buf.readInt();
                        switch (typeInt) {
                            case 0 -> {
                                return new HolderLocation();
                            }
                            case 1 -> {
                                long pos = buf.readLong();
                                Direction face = Direction.STREAM_CODEC.decode(buf);
                                return new HolderLocation(new ChunkPos(pos), face);
                            }
                            case 2 -> {
                                return new HolderLocation(UUIDUtil.STREAM_CODEC.decode(buf));
                            }
                        }
                        throw new RuntimeException("Invalid payload syntax.");
                    }
            );
}
