package io.github.daxigua2333.mocai_clues.data.sync.payload;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.UUID;

public record SyncSnapshotChunkPayload(
        UUID sessionId,
        UUID snapshotId,
        int chunkIndex,
        boolean lastChunk,
        List<ClueObject> objects
) implements CustomPacketPayload {

    public static final Type<SyncSnapshotChunkPayload> TYPE = new Type<>(SyncConstants.P_SNAPSHOT_CHUNK);

    private static final StreamCodec<ByteBuf, List<ClueObject>> LIST_CODEC =
            ClueObject.STREAM_CODEC.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, SyncSnapshotChunkPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncSnapshotChunkPayload::sessionId,
                    UUIDUtil.STREAM_CODEC, SyncSnapshotChunkPayload::snapshotId,
                    ByteBufCodecs.VAR_INT, SyncSnapshotChunkPayload::chunkIndex,
                    ByteBufCodecs.BOOL, SyncSnapshotChunkPayload::lastChunk,
                    LIST_CODEC, SyncSnapshotChunkPayload::objects,
                    SyncSnapshotChunkPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
