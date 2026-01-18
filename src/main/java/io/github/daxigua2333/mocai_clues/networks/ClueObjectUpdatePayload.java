package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;


public record ClueObjectUpdatePayload(Location location, Data data) implements CustomPacketPayload {
//    enum Mode {
//        UPSERT, DELETE;
//        // compress when stream codec this
//        private static final Mode[] VALUES = values();
//        public static Mode byId(int id) {
//            if (id < 0 || id >= VALUES.length)
//                throw new io.netty.handler.codec.DecoderException("Bad Mode id: " + id);
//            return VALUES[id];
//        }
//        public static final StreamCodec<ByteBuf, Mode> STREAM_CODEC =
//                ByteBufCodecs.VAR_INT.map(
//                        Mode::byId,      // decode: int -> enum
//                        Mode::ordinal    // encode: enum -> int
//                );
//    }

    public record Location(Type type, ChunkPos chunkPos, UUID entityId) {
        enum Type {
            SD, CHUNK, ENTITY;
        }

        public Location(ChunkPos pos) {
            this(Type.CHUNK, pos, null);
        }
        public Location(UUID entityId) {
            this(Type.ENTITY, null, entityId);
        }
        public Location() {
            this(Type.SD, null, null);
        }

        public static final StreamCodec<ByteBuf, Location> STREAM_CODEC =
                StreamCodec.of(
                        (buf, inst) -> {
                            switch (inst.type) {
                                case SD -> {
                                    buf.writeInt(0);
                                }
                                case CHUNK -> {
                                    buf.writeInt(1);
                                    buf.writeLong(inst.chunkPos.toLong());
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
                                    return new Location();
                                }
                                case 1 -> {
                                    return new Location(new ChunkPos(buf.readLong()));
                                }
                                case 2 -> {
                                    return new Location(UUIDUtil.STREAM_CODEC.decode(buf));
                                }
                            }
                            throw new RuntimeException("Invalid payload syntax.");
                        }
                );
    }

    public record Data(Mode mode, UUID id, ClueObject obj) {
        enum Mode {
            UPSERT, DELETE;
        }

        public Data(ClueObject obj) {
            this(Mode.UPSERT, null, obj);
        }
        public Data(UUID id) {
            this(Mode.DELETE, id, null);
        }

        public static final StreamCodec<ByteBuf, Data> STREAM_CODEC = StreamCodec.of(
                (buf, data) -> {
                    switch (data.mode) {
                        case UPSERT -> {
                            buf.writeBoolean(true);
                            ClueObject.STREAM_CODEC.encode(buf, data.obj);
                        }
                        case DELETE -> {
                            buf.writeBoolean(false);
                            UUIDUtil.STREAM_CODEC.encode(buf, data.id);
                        }
                    }
                },
                buf -> {
                    boolean mo = buf.readBoolean();
                    if (mo) {
                        return new Data(ClueObject.STREAM_CODEC.decode(buf));
                    } else {
                        return new Data(UUIDUtil.STREAM_CODEC.decode(buf));
                    }
                }
        );
    }

    public static final CustomPacketPayload.Type<ClueObjectUpdatePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "clue_object_update_payload"));

    public static final StreamCodec<ByteBuf, ClueObjectUpdatePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        Location.STREAM_CODEC.encode(buf, payload.location);
                        Data.STREAM_CODEC.encode(buf, payload.data);
                    },
                    buf -> {
                        return new ClueObjectUpdatePayload(
                                Location.STREAM_CODEC.decode(buf),
                                Data.STREAM_CODEC.decode(buf)
                        );
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}