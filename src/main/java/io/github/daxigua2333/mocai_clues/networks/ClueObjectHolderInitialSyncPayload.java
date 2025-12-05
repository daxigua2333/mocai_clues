package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClueObjectHolderInitialSyncPayload(ClueObject object) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClueObjectHolderInitialSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "clue_object_holder_initial_sync_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClueObjectHolderInitialSyncPayload> STREAM_CODEC =
            StreamCodec.of(
                    (RegistryFriendlyByteBuf buf, ClueObjectHolderInitialSyncPayload payload) -> {
                        ClueObject.STREAM_CODEC.encode(buf, payload.object);
                    },
                    buf -> {
                        return new ClueObjectHolderInitialSyncPayload(ClueObject.STREAM_CODEC.decode(buf));
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
