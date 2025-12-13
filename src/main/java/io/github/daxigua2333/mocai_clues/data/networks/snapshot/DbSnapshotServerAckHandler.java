package io.github.daxigua2333.mocai_clues.data.networks.snapshot;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class DbSnapshotServerAckHandler {
    private DbSnapshotServerAckHandler() {}

    public static void handleAck(DbSnapshotPayloads.DbSnapshotAckPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            // mark "client has full copy" here (so you can start sending deltas later)
            // e.g. ServerDbSyncState.onAck(player, payload.snapshotId());
        }
    }
}
