package io.github.daxigua2333.cmagic_clue.data.networks;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.data.ObjectHolder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClueObjectHolderFullPayload(ObjectHolder<ClueObject> data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClueObjectHolderFullPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "clue_object_holder_full_payload"));

    // Reuse your ObjectHolder full stream codec, then wrap it in the record
    public static final StreamCodec<ByteBuf, ClueObjectHolderFullPayload> STREAM_CODEC =
        ObjectHolder.streamCodec(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC)
            .map(ClueObjectHolderFullPayload::new, ClueObjectHolderFullPayload::data);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
