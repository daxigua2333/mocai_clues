package io.github.daxigua2333.mocai_clues.data.sync.payload;

import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SyncRequestOpsPayload(
        UUID sessionId,
        long fromSeqExclusive
) implements CustomPacketPayload {

    public static final Type<SyncRequestOpsPayload> TYPE = new Type<>(SyncConstants.P_REQUEST_OPS);

    public static final StreamCodec<ByteBuf, SyncRequestOpsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncRequestOpsPayload::sessionId,
                    ByteBufCodecs.VAR_LONG, SyncRequestOpsPayload::fromSeqExclusive,
                    SyncRequestOpsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
