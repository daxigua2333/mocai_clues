package io.github.daxigua2333.mocai_clues.data.sync.payload;

import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SyncSnapshotBeginPayload(
        UUID sessionId,
        UUID snapshotId,
        long baseSeq,
        int totalCount,
        int chunkSize
) implements CustomPacketPayload {

    public static final Type<SyncSnapshotBeginPayload> TYPE = new Type<>(SyncConstants.P_SNAPSHOT_BEGIN);

    public static final StreamCodec<ByteBuf, SyncSnapshotBeginPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncSnapshotBeginPayload::sessionId,
                    UUIDUtil.STREAM_CODEC, SyncSnapshotBeginPayload::snapshotId,
                    ByteBufCodecs.VAR_LONG, SyncSnapshotBeginPayload::baseSeq,
                    ByteBufCodecs.VAR_INT, SyncSnapshotBeginPayload::totalCount,
                    ByteBufCodecs.VAR_INT, SyncSnapshotBeginPayload::chunkSize,
                    SyncSnapshotBeginPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
