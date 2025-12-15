package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClueObjectUpsertPayload(ClueObject object) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClueObjectUpsertPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "clue_object_upsert_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClueObjectUpsertPayload> STREAM_CODEC =
            StreamCodec.of(
                    (RegistryFriendlyByteBuf buf, ClueObjectUpsertPayload payload) -> {
                        ClueObject.STREAM_CODEC.encode(buf, payload.object);
                    },
                    buf -> {
                        return new ClueObjectUpsertPayload(ClueObject.STREAM_CODEC.decode(buf));
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
