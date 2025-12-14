//package io.github.daxigua2333.mocai_clues.data.networks.delta;
//
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.codec.ByteBufCodecs;
//import net.minecraft.network.codec.StreamCodec;
//
//import java.util.UUID;
//
//public sealed interface DeltaOp permits DeltaOp.Upsert, DeltaOp.Delete {
//    int TYPE_UPSERT = 0;
//    int TYPE_DELETE = 1;
//
//    int typeId();
//
//    record Upsert(ClueObject value) implements DeltaOp {
//        @Override public int typeId() { return TYPE_UPSERT; }
//    }
//
//    record Delete(UUID id) implements DeltaOp {
//        @Override public int typeId() { return TYPE_DELETE; }
//    }
//
//    StreamCodec<RegistryFriendlyByteBuf, DeltaOp> STREAM_CODEC = new StreamCodec<>() {
//        @Override
//        public DeltaOp decode(RegistryFriendlyByteBuf buf) {
//            final int type = buf.readVarInt();
//            return switch (type) {
//                case TYPE_UPSERT -> new Upsert(ClueObject.STREAM_CODEC.decode(buf));
//                case TYPE_DELETE -> new Delete(ByteBufCodecs.UUID.decode(buf));
//                default -> throw new IllegalArgumentException("Unknown DeltaOp type: " + type);
//            };
//        }
//
//        @Override
//        public void encode(RegistryFriendlyByteBuf buf, DeltaOp op) {
//            buf.writeVarInt(op.typeId());
//            switch (op) {
//                case Upsert u -> ClueObject.STREAM_CODEC.encode(buf, u.value());
//                case Delete d -> ByteBufCodecs.UUID.encode(buf, d.id());
//            }
//        }
//    };
//}