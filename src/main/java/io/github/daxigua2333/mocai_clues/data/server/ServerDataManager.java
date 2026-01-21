package io.github.daxigua2333.mocai_clues.data.server;

import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.AttachedEntitySet;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.client.ClientIndexManager;
import io.github.daxigua2333.mocai_clues.data.common.IndexManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.*;
import java.util.function.Consumer;

// TODO: route to SD or something...
public final class ServerDataManager {

    // ======== retrieve =========
    // TODO: mutable and mark dirty issues......
    // idk.... maybe I ll still stick to this shit, or maybe turn to ESC in the future
    public record RetrieveResult(List<ClueObject> objects, Consumer<ClueObject> markDirty) {}

    public static List<RetrieveResult> retrieveByBlockPos(Level level, BlockPos pos) {

        List<RetrieveResult> result = new ArrayList<>();
        // chunk attach
        IAttachmentHolder chunk = level.getChunkAt(pos);
        ObjectHolder<ClueObject> holder = chunk.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
        result.add(new RetrieveResult(IndexManager.byBlockPos(holder, pos), obj -> {
            holder.markDirty(obj);
            chunk.syncData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
        }));
        // saved data
        var server = level.getServer();
        if (server != null) {
            ClueObjectHolderInSavedData savedData = ClueObjectHolderInSavedData.getInstance(server);
            ObjectHolder<ClueObject> holder1 = savedData.holder();
            result.add(new RetrieveResult(IndexManager.byBlockPos(holder1, pos), obj -> {
                holder1.markDirty(obj);
                savedData.setDirty();
            }));
        }

        return result;
    }
    public static List<RetrieveResult> retrieveByEntity(Entity entity) {
        List<RetrieveResult> result = new ArrayList<>();
        // entity attachment
        ObjectHolder<ClueObject> holder = entity.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
        result.add(new RetrieveResult(new ArrayList<>(holder.values()), obj -> {
            holder.markDirty(obj);
            entity.syncData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
        }));

        // TODO: optimize: I think there is no need to boost this by index
        var server = entity.getServer();
        if (server != null) {
            ClueObjectHolderInSavedData savedData = ClueObjectHolderInSavedData.getInstance(server);
            ObjectHolder<ClueObject> holder1 = savedData.holder();

            List<ClueObject> objs = new ArrayList<>();
            for (ClueObject obj : holder1.values()) {
                AttachedEntitySet compo = obj.getComponent(ComponentType.ATTACHED_ENTITY_SET);
                if (compo == null) continue;
                if (compo.getImmutable().contains(entity.getUUID())) {
                    objs.add(obj);
                }
            }

            result.add(new RetrieveResult(objs, obj -> {
                holder1.markDirty(obj);
                savedData.setDirty();
            }));
        }

        return result;
    }


    // ========== upsert ==========
    public static void upsert(MinecraftServer server, ClueObject obj) {
        ClueObjectHolderInSavedData.getInstance(server).put(obj);
    }
    public static void upsert(ServerLevel level, ChunkPos pos, ClueObject obj) {
        LevelChunk chunk = level.getChunk(pos.x, pos.z);
        // TODO: optimize: avoid loading unloaded chunks, maybe implement own cache pool
        ObjectHolder<ClueObject> holder = chunk.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
        holder.put(obj);
    }
    public static void upsert(ServerLevel level, UUID entityId, ClueObject obj) {
        Entity entity = level.getEntity(entityId);
        if (entity == null) return;
        ObjectHolder<ClueObject> holder = entity.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
        holder.put(obj);
    }


    public static void delete(MinecraftServer server, UUID id) {
        ClueObjectHolderInSavedData.getInstance(server).remove(id);
    }
    public static void delete(ServerLevel level, ChunkPos pos, UUID id) {
        LevelChunk chunk = level.getChunk(pos.x, pos.z);
        ObjectHolder<ClueObject> holder = chunk.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
        holder.remove(id);
    }
    public static void delete(ServerLevel level, UUID entityId, UUID id) {
        Entity entity = level.getEntity(entityId);
        if (entity == null) return;
        ObjectHolder<ClueObject> holder = entity.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
        holder.remove(id);
    }
}
