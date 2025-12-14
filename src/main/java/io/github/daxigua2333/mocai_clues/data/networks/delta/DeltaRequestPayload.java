package io.github.daxigua2333.mocai_clues.data.networks.delta;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DeltaRequestPayload(long fromSeq) implements CustomPacketPayload {
    public static final Type<DeltaRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "db_delta_request"));

    public static final StreamCodec<ByteBuf, DeltaRequestPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG, DeltaRequestPayload::fromSeq,
                    DeltaRequestPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}