package io.github.daxigua2333.mocai_clues.data.sync;

import net.minecraft.resources.ResourceLocation;

public final class SyncConstants {
    private SyncConstants() {}

    public static final String MODID = "mocai_clues";
    public static final String PROTOCOL_VERSION = "1";

    public static final ResourceLocation DATASET_ID = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object");

    // Payload IDs
    public static final ResourceLocation P_HELLO = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_hello");
    public static final ResourceLocation P_START = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_start");
    public static final ResourceLocation P_SNAPSHOT_BEGIN = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_snapshot_begin");
    public static final ResourceLocation P_SNAPSHOT_CHUNK = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_snapshot_chunk");
    public static final ResourceLocation P_SNAPSHOT_END = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_snapshot_end");
    public static final ResourceLocation P_REQUEST_OPS = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_request_ops");
    public static final ResourceLocation P_OP_BATCH = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_op_batch");
    public static final ResourceLocation P_ACK = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_ack");
    public static final ResourceLocation P_ERROR = ResourceLocation.fromNamespaceAndPath(MODID, "clue_object_sync_error");

    // Safety limits (clientbound payloads must be <= 1 MiB; keep chunks conservative)
    public static final int MAX_SNAPSHOT_OBJS_PER_CHUNK = 128;
    public static final int MAX_OPS_PER_BATCH = 256;

    // Server pump limits
    public static final int MAX_CHUNKS_PER_TICK_PER_PLAYER = 1;
    public static final int MAX_OP_BATCHES_PER_TICK_PER_PLAYER = 2;

    // Oplog retention
    public static final long OPLOG_MAX_ENTRIES = 200_000L;
    public static final long OPLOG_MIN_ENTRIES = 10_000L;
}
