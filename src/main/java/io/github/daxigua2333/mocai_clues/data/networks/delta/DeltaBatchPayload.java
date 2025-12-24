//package io.github.daxigua2333.mocai_clues.data.networks.delta;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.codec.ByteBufCodecs;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public record DeltaBatchPayload(long fromSeq, List<DeltaOp> ops) implements CustomPacketPayload {
//    public static final Type<DeltaBatchPayload> TYPE =
//            new Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "db_delta_batch"));
//
//    public static final StreamCodec<RegistryFriendlyByteBuf, DeltaBatchPayload> STREAM_CODEC =
//            StreamCodec.composite(
//                    ByteBufCodecs.VAR_LONG, DeltaBatchPayload::fromSeq,
//                    ByteBufCodecs.collection(ArrayList::new, DeltaOp.STREAM_CODEC), DeltaBatchPayload::ops,
//                    DeltaBatchPayload::new
//            );
//
//    public long toSeqInclusive() {
//        return ops.isEmpty() ? (fromSeq - 1) : (fromSeq + ops.size() - 1);
//    }
//
//    @Override
//    public Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
//}