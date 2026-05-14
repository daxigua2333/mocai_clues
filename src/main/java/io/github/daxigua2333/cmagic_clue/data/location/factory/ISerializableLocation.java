package io.github.daxigua2333.cmagic_clue.data.location.factory;

import io.github.daxigua2333.cmagic_clue.data.location.IRuntimeLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Serializable version of Location (factory pattern), used for networking
 * USAGE:
 * - Payload record holds the ILocationSerializable
 * - Create StreamCodec with ILocationSerializable.Type.DISPATCH_CODEC
 * - In handle(payload, context), use ILocationSerializable#create(context) to create the runtime Location
 */
public interface ISerializableLocation {
    enum Type {
        CHUNK(FromChunkAttachmentSerializable.STREAM_CODEC),
        ENTITY(FromEntityAttachmentSerializable.STREAM_CODEC),
//        SERVER_SD(FromServerSavedDataSerializable.STREAM_CODEC),
//        CLIENT_SD(FromClientSavedDataSerializable.STREAM_CODEC),
        SD(FromSavedDataSerializable.STREAM_CODEC),
        CLUE_BOOK(FromClueBookSerializable.STREAM_CODEC)
        ;

        private final StreamCodec<ByteBuf, ? extends ISerializableLocation> streamCodec;

        Type(StreamCodec<ByteBuf, ? extends ISerializableLocation> streamCodec) {
            this.streamCodec = streamCodec;
        }

        public static final StreamCodec<ByteBuf, ISerializableLocation> DISPATCH_CODEC = new StreamCodec<>() {
            @Override
            public ISerializableLocation decode(ByteBuf buf) {
                // 1. Read the ID (Ordinal or Name)
//                int id = buf.readVarInt();
                int id = ByteBufCodecs.VAR_INT.decode(buf);
                Type type = Type.values()[id];

                // 2. Use the specific codec to read the rest
                return type.streamCodec.decode(buf);
            }

            @Override
            public void encode(ByteBuf buf, ISerializableLocation value) {
                // 1. Write the ID
                Type type = value.type();
//                buf.writeVarInt(type.ordinal());
                ByteBufCodecs.VAR_INT.encode(buf, type.ordinal());

                // 2. Cast and write data (Safe because getType guarantees match)
                ((StreamCodec) type.streamCodec).encode(buf, value);
            }
        };
    }

    Type type();

    IRuntimeLocation create(IPayloadContext context);

}
