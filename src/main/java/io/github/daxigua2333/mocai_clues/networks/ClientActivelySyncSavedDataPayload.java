package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientActivelySyncSavedDataPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientActivelySyncSavedDataPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "client_actively_sync_saved_data_payload"));

    public static final StreamCodec<ByteBuf, ClientActivelySyncSavedDataPayload> STREAM_CODEC = StreamCodec.unit(new ClientActivelySyncSavedDataPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
