//package io.github.daxigua2333.mocai_clues.networks;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import net.minecraft.core.UUIDUtil;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.UUID;
//
//public record ClueObjectDeletePayload(UUID id) implements CustomPacketPayload {
//    public static final CustomPacketPayload.Type<ClueObjectDeletePayload> TYPE = new CustomPacketPayload.Type<>(
//            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "clue_object_delete_payload"));
//
//    public static final StreamCodec<RegistryFriendlyByteBuf, ClueObjectDeletePayload> STREAM_CODEC =
//            StreamCodec.composite(
//                    UUIDUtil.STREAM_CODEC, ClueObjectDeletePayload::id,
//                    ClueObjectDeletePayload::new
//            );
//
//    @Override
//    public Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
//}
