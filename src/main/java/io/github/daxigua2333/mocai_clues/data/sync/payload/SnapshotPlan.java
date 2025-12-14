package io.github.daxigua2333.mocai_clues.data.sync.payload;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record SnapshotPlan(
        boolean required,
        UUID snapshotId,
        long baseSeq
) {
    public static final UUID NIL_UUID = new UUID(0L, 0L);

    public static final StreamCodec<ByteBuf, SnapshotPlan> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SnapshotPlan::required,
                    UUIDUtil.STREAM_CODEC, SnapshotPlan::snapshotId,
                    ByteBufCodecs.VAR_LONG, SnapshotPlan::baseSeq,
                    SnapshotPlan::new
            );

    public static SnapshotPlan none() {
        return new SnapshotPlan(false, NIL_UUID, 0L);
    }

    public static SnapshotPlan required(UUID snapshotId, long baseSeq) {
        return new SnapshotPlan(true, snapshotId, baseSeq);
    }
}
