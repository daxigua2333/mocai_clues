package io.github.daxigua2333.mocai_clues.data.sync.payload;

import io.github.daxigua2333.mocai_clues.data.sync.MyObjectOpRecord;
import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.UUID;

public record SyncOpBatchPayload(
        UUID sessionId,
        long fromSeqExclusive,
        long toSeqInclusive,
        List<MyObjectOpRecord> ops
) implements CustomPacketPayload {

    public static final Type<SyncOpBatchPayload> TYPE = new Type<>(SyncConstants.P_OP_BATCH);

    private static final StreamCodec<ByteBuf, List<MyObjectOpRecord>> LIST_CODEC =
            MyObjectOpRecord.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, SyncOpBatchPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncOpBatchPayload::sessionId,
                    ByteBufCodecs.VAR_LONG, SyncOpBatchPayload::fromSeqExclusive,
                    ByteBufCodecs.VAR_LONG, SyncOpBatchPayload::toSeqInclusive,
                    LIST_CODEC, SyncOpBatchPayload::ops,
                    SyncOpBatchPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
