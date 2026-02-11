//package io.github.daxigua2333.mocai_clues.data.server;
//
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
//import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
//import io.github.daxigua2333.mocai_clues.data.common.IndexManager;
//import io.github.daxigua2333.mocai_clues.data.common.RetrieveResult;
//import io.github.daxigua2333.mocai_clues.data.location.FromChunkAttachment;
//import io.github.daxigua2333.mocai_clues.data.location.FromEntityAttachment;
//import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
//import net.minecraft.core.BlockPos;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.chunk.LevelChunk;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//// TODO: route to SD or something...
//public final class ServerDataManager {
//
//    // ======== retrieve =========
//    // TODO: mutable and mark dirty issues......
//    // idk.... maybe I ll still stick to this shit, or maybe turn to ESC in the future
////    public record RetrieveResult(List<ClueObject> objects, Consumer<ClueObject> markDirty) {
////    }
//
//    public static List<RetrieveResult> retrieveByBlockPos(Level level, BlockPos pos) {
//        List<RetrieveResult> result = new ArrayList<>();
//        // chunk attach
//        IRuntimeLocation fromChunk = new FromChunkAttachment(level, pos);
//        result.add(new RetrieveResult(
//                fromChunk,
//                IndexManager.byBlockPos(fromChunk.getHolder(), pos)
//        ));
//
//        return result;
//    }
//
//    public static List<RetrieveResult> retrieveByEntity(Entity entity) {
//        List<RetrieveResult> result = new ArrayList<>();
//        // entity attachment
//        IRuntimeLocation fromEntity = new FromEntityAttachment(entity);
//        result.add(new RetrieveResult(
//                fromEntity,
//                new ArrayList<>(fromEntity.getHolder().values())
//        ));
//
//
//        return result;
//    }
//
//
//    // ========== upsert ==========
//    public static void upsert(IRuntimeLocation location, ClueObject obj) {
//        location.getHolder().put(obj);
//        location.markDirty();
//    }
//
//    public static void delete(IRuntimeLocation location, UUID id) {
//        location.getHolder().remove(id);
//        location.markDirty();
//    }
//
//    @Deprecated
//    public static void upsert(MinecraftServer server, ClueObject obj) {
//        ClueObjectHolderInSavedData.getInstance(server).put(obj);
//    }
//
//    @Deprecated
//    public static void upsert(ServerLevel level, ChunkPos pos, ClueObject obj) {
//        LevelChunk chunk = level.getChunk(pos.x, pos.z);
//        // TODO: optimize: avoid loading unloaded chunks, maybe implement own cache pool
//        ObjectHolder<ClueObject> holder = chunk.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
//        holder.put(obj);
//    }
//
//    @Deprecated
//    public static void upsert(ServerLevel level, UUID entityId, ClueObject obj) {
//        Entity entity = level.getEntity(entityId);
//        if (entity == null) return;
//        ObjectHolder<ClueObject> holder = entity.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
//        holder.put(obj);
//    }
//
//
//    @Deprecated
//    public static void delete(MinecraftServer server, UUID id) {
//        ClueObjectHolderInSavedData.getInstance(server).remove(id);
//    }
//
//    @Deprecated
//    public static void delete(ServerLevel level, ChunkPos pos, UUID id) {
//        LevelChunk chunk = level.getChunk(pos.x, pos.z);
//        ObjectHolder<ClueObject> holder = chunk.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
//        holder.remove(id);
//    }
//
//    @Deprecated
//    public static void delete(ServerLevel level, UUID entityId, UUID id) {
//        Entity entity = level.getEntity(entityId);
//        if (entity == null) return;
//        ObjectHolder<ClueObject> holder = entity.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
//        holder.remove(id);
//    }
//}
