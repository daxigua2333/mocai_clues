package io.github.daxigua2333.mocai_clues.data.server.api;

import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.AttachedEntitySet;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO: route to SD or something...
public final class ServerDataAccessor {

    // ======== retrieve =========
    // TODO: mutable and mark dirty issues......
    public static ClueObject retrieveByIDInSD(MinecraftServer server, UUID id) {
        return ClueObjectHolderInSavedData.getInstance(server).holder().get(id);
    }
    public static List<ClueObject> retrieveByBlockPos(Level level, BlockPos pos) {
        // chunk attach
        List<ClueObject> result = new ArrayList<>(level.getChunkAt(pos).getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER).values());

        // saved data
        var server = level.getServer();
        if (server == null) {
            var holder = ClueObjectHolderInSavedData.getInstance(server).holder();
            for (var obj : holder.values()) {
                BlockPosSet compo = obj.getComponent(ComponentType.BLOCK_POS_SET);
                if (compo == null) continue;
                if (compo.getImmutable().contains(pos)) {
                    result.add(obj);
                }
            }
        }

        return result;
    }
    public static List<ClueObject> retrieveByEntity(Entity entity) {
        // entity attachment
        List<ClueObject> result = new ArrayList<>(entity.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER).values());

        // TODO: optimize: I think this has no need to boost by index
        var server = entity.getServer();
        if (server != null) {
            ObjectHolder<ClueObject> holder = ClueObjectHolderInSavedData.getInstance(server).holder();
            for (ClueObject obj : holder.values()) {
                AttachedEntitySet compo = obj.getComponent(ComponentType.ATTACHED_ENTITY_SET);
                if (compo == null) continue;
                if (compo.getImmutable().contains(entity.getUUID())) {
                    result.add(obj);
                }
            }
        }

        return result;
    }


    // ========== upsert ==========

    public static void createDefault(Level level) {
        var obj = Assembler.createManualClue();
        var holder = ClueObjectHolderInSavedData.getInstance(level.getServer());
        holder.put(obj);
    }

    public static void upsert(MinecraftServer server, ClueObject obj) {
        ClueObjectHolderInSavedData.getInstance(server).put(obj);
    }
    public static void upsert(ServerLevel level, ChunkPos pos, ClueObject obj) {
        LevelChunk chunk = level.getChunk(pos.x, pos.z);
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
