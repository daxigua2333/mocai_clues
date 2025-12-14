package io.github.daxigua2333.mocai_clues.data.sync.server;


import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.SyncConstants;
import io.github.daxigua2333.mocai_clues.data.sync.payload.SyncAckPayload;
import io.github.daxigua2333.mocai_clues.data.sync.payload.SyncHelloPayload;
import io.github.daxigua2333.mocai_clues.data.sync.payload.SyncRequestOpsPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class MyObjectSyncServerPayloadHandler {
    private MyObjectSyncServerPayloadHandler() {}

    public static void handleHello(final SyncHelloPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isServerInitialized()) return;
        if (!SyncConstants.DATASET_ID.equals(payload.dataset())) return;

        ServerPlayer player = (ServerPlayer) context.player();
        try {
            MyObjectSync.server().handleHello(player, payload);
        } catch (Exception e) {
            context.disconnect(Component.literal("Sync failed (hello): " + e.getMessage()));
        }
    }

    public static void handleRequestOps(final SyncRequestOpsPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isServerInitialized()) return;
        ServerPlayer player = (ServerPlayer) context.player();
        try {
            MyObjectSync.server().handleRequestOps(player, payload);
        } catch (Exception e) {
            context.disconnect(Component.literal("Sync failed (request): " + e.getMessage()));
        }
    }

    public static void handleAck(final SyncAckPayload payload, final IPayloadContext context) {
        if (!MyObjectSync.isServerInitialized()) return;
        ServerPlayer player = (ServerPlayer) context.player();
        try {
            MyObjectSync.server().handleAck(player, payload);
        } catch (Exception e) {
            context.disconnect(Component.literal("Sync failed (ack): " + e.getMessage()));
        }
    }
}
