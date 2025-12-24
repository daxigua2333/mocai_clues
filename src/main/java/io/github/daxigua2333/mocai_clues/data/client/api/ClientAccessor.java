package io.github.daxigua2333.mocai_clues.data.client.api;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.world.data.WorldBlockPos;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BlockOutlineRenderer;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.client.ClientDatabase;
import io.github.daxigua2333.mocai_clues.data.client.ClientObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.NitriteMyObjectStore;
import io.github.daxigua2333.mocai_clues.entry.client.render.ObjectRenderManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.FluentFilter;

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

    public static List<ClueObject> queryClueObjectByChunkPos(ChunkPos pos) {  // TODO:
        List<ClueObject> result = new ArrayList<>();

        var holder = ClientObjectHolderInSavedData.getInstance().getHolder();
        for (var obj : holder.values()) {
            for(var compo : obj.getComponents()) {
                if (compo instanceof WorldBlockPos posCompo && pos.equals(new ChunkPos(posCompo.getBlockPos()))) {
                    result.add(obj);
                    break;
                }
            }
        }

        return result;
    }

    public static Set<ChunkPos> queryChunkPosInSavedData() {
        Set<ChunkPos> result = new HashSet<>();

        var holder = ClientObjectHolderInSavedData.getInstance().getHolder();
        for (ClueObject obj : holder.values()) {
            for (var compo : obj.getComponents()) {
                if (compo instanceof WorldBlockPos posCompo) {
                    result.add(new ChunkPos(posCompo.getBlockPos()));
                }
            }
        }

        return result;
    }

}
