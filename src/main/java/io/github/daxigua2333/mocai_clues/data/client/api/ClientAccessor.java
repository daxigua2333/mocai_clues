package io.github.daxigua2333.mocai_clues.data.client.api;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosList;
import io.github.daxigua2333.mocai_clues.data.client.ClientObjectHolderInSavedData;
import net.minecraft.world.level.ChunkPos;

import java.util.*;

/**
 * entry to access all the client data
 */
public class ClientAccessor {

    public static List<ClueObject> queryClueObjectByClueType(ClueType type) {
        List<ClueObject> result = new ArrayList<>();
        result.addAll(ClientSavedDataAccessor.queryClueObjectByClueType(type));
//        result.addAll();
//        NitriteMyObjectStore store = MyObjectSync.client().store();
//        String field = NitriteMyObjectStore.clueField("type");
//        result = store.retrieve(FluentFilter.where(field).eq(type.toString()));
        return result;
    }

    public static List<ClueObject> queryClueObjectByChunkPos(ChunkPos chunkPos) {  // TODO: optimize
        List<ClueObject> result = new ArrayList<>();

        var holder = ClientObjectHolderInSavedData.getInstance().getHolder();
        for (var obj : holder.values()) {
            BlockPosList compo = obj.getComponent(ComponentType.BLOCK_POS_LIST);
            if (compo == null) continue;
            for (var pos : compo.getImmutable()) {
                if (chunkPos.equals(new ChunkPos(pos))) {
                    result.add(obj);
                    break;
                }
            }
        }

        return result;
    }

    public static Set<ChunkPos> queryChunkPosInSavedData() {  // TODO: optimize
        Set<ChunkPos> result = new HashSet<>();

        var holder = ClientObjectHolderInSavedData.getInstance().getHolder();
        for (ClueObject obj : holder.values()) {
            BlockPosList compo = obj.getComponent(ComponentType.BLOCK_POS_LIST);
            if (compo == null) continue;
            for (var pos : compo.getImmutable()) {
                result.add(new ChunkPos(pos));
            }
        }

        return result;
    }

}
