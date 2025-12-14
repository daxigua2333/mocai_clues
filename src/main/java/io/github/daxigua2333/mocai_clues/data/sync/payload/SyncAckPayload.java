package io.github.daxigua2333.mocai_clues.data.sync.payload;

import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.UUID;

public record SyncAckPayload(
        UUID sessionId,
        long lastAppliedSeq
) implements CustomPacketPayload {

    public static final Type<SyncAckPayload> TYPE = new Type<>(SyncConstants.P_ACK);

    public static final StreamCodec<ByteBuf, SyncAckPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncAckPayload::sessionId,
                    ByteBufCodecs.VAR_LONG, SyncAckPayload::lastAppliedSeq,
                    SyncAckPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
