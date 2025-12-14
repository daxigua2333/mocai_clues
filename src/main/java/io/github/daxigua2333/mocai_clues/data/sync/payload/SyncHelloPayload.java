package io.github.daxigua2333.mocai_clues.data.sync.payload;


import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncHelloPayload(
        ResourceLocation dataset,
        long clientLastAppliedSeq,
        int clientProtocol
) implements CustomPacketPayload {

    public static final Type<SyncHelloPayload> TYPE = new Type<>(SyncConstants.P_HELLO);

    public static final StreamCodec<ByteBuf, SyncHelloPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SyncHelloPayload::dataset,
                    ByteBufCodecs.VAR_LONG, SyncHelloPayload::clientLastAppliedSeq,
                    ByteBufCodecs.VAR_INT, SyncHelloPayload::clientProtocol,
                    SyncHelloPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
