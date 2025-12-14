package io.github.daxigua2333.mocai_clues.data.sync.payload;

import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SyncErrorPayload(
        UUID sessionId,
        int code,
        String message
) implements CustomPacketPayload {

    public static final Type<SyncErrorPayload> TYPE = new Type<>(SyncConstants.P_ERROR);

    public static final StreamCodec<ByteBuf, SyncErrorPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncErrorPayload::sessionId,
                    ByteBufCodecs.VAR_INT, SyncErrorPayload::code,
                    ByteBufCodecs.STRING_UTF8, SyncErrorPayload::message,
                    SyncErrorPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
