//package io.github.daxigua2333.mocai_clues.data.networks.snapshot;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.codec.ByteBufCodecs;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.List;
//import java.util.UUID;
//
//public final class DbSnapshotPayloads {
//    private DbSnapshotPayloads() {}
//
//    private static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_CODEC =
////        UUIDUtil.STREAM_CODEC;
//        StreamCodec.composite(
//            ByteBufCodecs.VAR_LONG, UUID::getMostSignificantBits,
//            ByteBufCodecs.VAR_LONG, UUID::getLeastSignificantBits,
//            UUID::new
//        );
//
//    public static final int MAX_OBJECTS_PER_CHUNK = 256;
//
//    public record DbSnapshotStartPayload(UUID snapshotId, int totalObjects, int totalChunks) implements CustomPacketPayload {
//        public static final Type<DbSnapshotStartPayload> TYPE =
//            new Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "db_snapshot_start"));
//
//        public static final StreamCodec<RegistryFriendlyByteBuf, DbSnapshotStartPayload> STREAM_CODEC =
//            StreamCodec.composite(
//                UUID_CODEC, DbSnapshotStartPayload::snapshotId,
//                ByteBufCodecs.VAR_INT, DbSnapshotStartPayload::totalObjects,
//                ByteBufCodecs.VAR_INT, DbSnapshotStartPayload::totalChunks,
//                DbSnapshotStartPayload::new
//            );
//
//        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
//    }
//
//    public record DbSnapshotChunkPayload(UUID snapshotId, int chunkIndex, boolean last, List<ClueObject> objects) implements CustomPacketPayload {
//        public static final Type<DbSnapshotChunkPayload> TYPE =
//            new Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "db_snapshot_chunk"));
//
//        private static final StreamCodec<RegistryFriendlyByteBuf, List<ClueObject>> OBJECT_LIST_CODEC =
////            ByteBufCodecs.list(MAX_OBJECTS_PER_CHUNK).apply(ClueObject.STREAM_CODEC);
//            ClueObject.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_OBJECTS_PER_CHUNK));
//
//        public static final StreamCodec<RegistryFriendlyByteBuf, DbSnapshotChunkPayload> STREAM_CODEC =
//            StreamCodec.composite(
//                UUID_CODEC, DbSnapshotChunkPayload::snapshotId,
//                ByteBufCodecs.VAR_INT, DbSnapshotChunkPayload::chunkIndex,
//                ByteBufCodecs.BOOL, DbSnapshotChunkPayload::last,
//                OBJECT_LIST_CODEC, DbSnapshotChunkPayload::objects,
//                DbSnapshotChunkPayload::new
//            );
//
//        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
//    }
//
//    public record DbSnapshotAckPayload(UUID snapshotId) implements CustomPacketPayload {
//        public static final Type<DbSnapshotAckPayload> TYPE =
//            new Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "db_snapshot_ack"));
//
//        public static final StreamCodec<RegistryFriendlyByteBuf, DbSnapshotAckPayload> STREAM_CODEC =
//            StreamCodec.composite(
//                UUID_CODEC, DbSnapshotAckPayload::snapshotId,
//                DbSnapshotAckPayload::new
//            );
//
//        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
//    }
//}
