//package io.github.daxigua2333.mocai_clues.data.networks;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//import net.minecraft.resources.ResourceLocation;
//
//
//public record ClueObjectHolderDeltaPayload(ObjectHolder.DeltaPayload<ClueObject> deltaPayload) implements CustomPacketPayload {
//    public static final CustomPacketPayload.Type<ClueObjectHolderDeltaPayload> TYPE = new CustomPacketPayload.Type<>(
//            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "clue_object_holder_delta_payload"));
//
//    public static final StreamCodec<RegistryFriendlyByteBuf, ClueObjectHolderDeltaPayload> STREAM_CODEC =
//            StreamCodec.of(
//                    (buf, payload) -> ObjectHolder.DeltaPayload.deltaStreamCodec(ClueObject.STREAM_CODEC).encode(buf, payload.deltaPayload()),
//                    buf -> new ClueObjectHolderDeltaPayload(ObjectHolder.DeltaPayload.deltaStreamCodec(ClueObject.STREAM_CODEC).decode(buf))
//            );
//
//    @Override
//    public Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
//}
