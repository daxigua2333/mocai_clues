package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ManualClueCreatePayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ManualClueCreatePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "manual_clue_create_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ManualClueCreatePayload> STREAM_CODEC =
            StreamCodec.unit(new ManualClueCreatePayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
