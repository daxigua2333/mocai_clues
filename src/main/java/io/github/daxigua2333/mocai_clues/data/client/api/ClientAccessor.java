package io.github.daxigua2333.mocai_clues.data.client.api;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.AttachedEntitySet;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.client.ClientIndexManager;
import io.github.daxigua2333.mocai_clues.data.client.ClientObjectHolderInSavedData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

/**
 * entry to access all the client data
 */
public class ClientAccessor {

    // TODO:: some route
    public static List<ClueObject> retrieveAllSavedData() {
        return new ArrayList<>(ClientObjectHolderInSavedData.getInstance().getHolder().values());
    }
    // TODO: also some distance culling, and traverse nearby chunks and get attachments

    // very frequent query in discovery system
    @SuppressWarnings("unchecked")
    public static List<ClueObject> retrieveByBlockPos(BlockPos pos) {
        List<ClueObject> result = new ArrayList<>();

        // chunk attachment
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            LevelChunk chunk = level.getChunkAt(pos);
            ObjectHolder<ClueObject> holder = chunk.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
            var index = (ObjectHolder<ClueObject>.Index<BlockPos>) holder.getIndex(ClientIndexManager.BY_BLOCK_POS);
            if (index != null) {
                result.addAll(index.values(pos));
            }
        }
        // saved data
        ObjectHolder<ClueObject> holder = ClientObjectHolderInSavedData.getInstance().getHolder();
        var index = (ObjectHolder<ClueObject>.Index<BlockPos>) holder.getIndex(ClientIndexManager.BY_BLOCK_POS);
        if (index != null) {
            result.addAll(index.values(pos));
        }

        return result;
    }

    // very frequent query in render system, building batch mesh
    @SuppressWarnings("unchecked")
    public static List<ClueObject> retrieveByChunkPos(ChunkPos chunkPos) {
        List<ClueObject> result = new ArrayList<>();

        // chunk
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
            result.addAll(chunk.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER).values());
        }

        // saved data
        var holder = ClientObjectHolderInSavedData.getInstance().getHolder();
        var index = (ObjectHolder<ClueObject>.Index<ChunkPos>) holder.getIndex(ClientIndexManager.BY_CHUNK_POS);
        if (index != null) {
            result.addAll(index.values(chunkPos));
        }

        return result;
    }

    public static List<ClueObject> retrieveByEntity(Entity entity) {
        // entity attachment
        List<ClueObject> result = new ArrayList<>(entity.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER).values());

        // saved data, TODO: optimize: I think this has no need to boost by index
        ObjectHolder<ClueObject> holder = ClientObjectHolderInSavedData.getInstance().getHolder();
        for (ClueObject obj : holder.values()) {
            AttachedEntitySet compo = obj.getComponent(ComponentType.ATTACHED_ENTITY_SET);
            if (compo == null) continue;
            if (compo.getImmutable().contains(entity.getUUID())) {
                result.add(obj);
            }
        }

        return result;
    }


}
