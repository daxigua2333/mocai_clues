package io.github.daxigua2333.mocai_clues.data.sync.payload;

import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SyncSnapshotEndPayload(
        UUID sessionId,
        UUID snapshotId,
        long baseSeq
) implements CustomPacketPayload {

    public static final Type<SyncSnapshotEndPayload> TYPE = new Type<>(SyncConstants.P_SNAPSHOT_END);

    public static final StreamCodec<ByteBuf, SyncSnapshotEndPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncSnapshotEndPayload::sessionId,
                    UUIDUtil.STREAM_CODEC, SyncSnapshotEndPayload::snapshotId,
                    ByteBufCodecs.VAR_LONG, SyncSnapshotEndPayload::baseSeq,
                    SyncSnapshotEndPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
