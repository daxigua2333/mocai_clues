package io.github.daxigua2333.cmagic_clue.networks;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.data.common.DataManager;
import io.github.daxigua2333.cmagic_clue.data.location.IRuntimeLocation;
import io.github.daxigua2333.cmagic_clue.data.location.factory.ISerializableLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;


public record ClueObjectUpdatePayload(ISerializableLocation location, Data data) implements CustomPacketPayload {
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

    // Shouldn't use generic because it's hard to serialize

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
            ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "clue_object_update_payload"));

    public static final StreamCodec<ByteBuf, ClueObjectUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            ISerializableLocation.Type.DISPATCH_CODEC, ClueObjectUpdatePayload::location,
            Data.STREAM_CODEC, ClueObjectUpdatePayload::data,
            ClueObjectUpdatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClueObjectUpdatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            IRuntimeLocation runtimeLocation = payload.location().create(context);
            Data data = payload.data();
            switch (data.mode()) {
                case DELETE -> DataManager.Server.delete(runtimeLocation, data.id);
                case UPSERT -> DataManager.Server.upsert(runtimeLocation, data.obj);
            }
        });
    }
}