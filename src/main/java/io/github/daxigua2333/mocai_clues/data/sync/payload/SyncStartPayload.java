package io.github.daxigua2333.mocai_clues.data.sync.payload;


import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SyncStartPayload(
        UUID sessionId,
        long serverSeq,
        long minSeqExclusive,
        SnapshotPlan snapshotPlan
) implements CustomPacketPayload {

    public static final Type<SyncStartPayload> TYPE = new Type<>(SyncConstants.P_START);

    public static final StreamCodec<ByteBuf, SyncStartPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, SyncStartPayload::sessionId,
                    ByteBufCodecs.VAR_LONG, SyncStartPayload::serverSeq,
                    ByteBufCodecs.VAR_LONG, SyncStartPayload::minSeqExclusive,
                    SnapshotPlan.STREAM_CODEC, SyncStartPayload::snapshotPlan,
                    SyncStartPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
