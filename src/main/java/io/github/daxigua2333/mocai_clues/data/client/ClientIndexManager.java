package io.github.daxigua2333.mocai_clues.data.client;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** client index schema(whenever a holder is newed): when init syncing */
public final class ClientIndexManager {
    public static final String BY_BLOCK_POS = "by_block_pos";
    public static final String BY_CHUNK_POS = "by_chunk_pos";
    public static final String BY_PASS_TYPE = "by_pass_type";

    private static void commonEnsure(ObjectHolder<ClueObject> holder) {
//        egs:
//        if (holder.getIndex().isEmpty()) {
//            holder.createUniqueIndex();
//        }
        if (holder.getIndex(BY_BLOCK_POS) == null) {
            holder.createMultiIndex(BY_BLOCK_POS, obj -> {
                BlockPosSet compo = obj.getComponent(ComponentType.BLOCK_POS_SET);
                if (compo == null) {
                    return List.of();
                }
                return compo.getImmutable();
            });
        }
    }

    // TODO: 2 unsafe type declare... but if there is nothing other than ClueObject, it is safe
    @SuppressWarnings("unchecked")
    public static <T> void attachmentHolderEnsure(ObjectHolder<T> tHolder) {
        ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
        commonEnsure(holder);

        if (holder.getIndex(BY_PASS_TYPE) == null) {
            holder.createMultiIndex(BY_PASS_TYPE, obj -> {
                Set<PassType> result = new HashSet<>();
                RendererHolder rCompo = obj.getComponent(ComponentType.RENDERER_HOLDER);
                if (rCompo == null) return List.of();
                rCompo.getImmutable().forEach(e -> result.add(e.getPassType()));
                return result;
            });
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void savedDataEnsure(ObjectHolder<T> tHolder) {
        ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
        commonEnsure(holder);

        if (holder.getIndex(BY_CHUNK_POS) == null) {
            holder.createMultiIndex(BY_CHUNK_POS, obj -> {
                BlockPosSet compo = obj.getComponent(ComponentType.BLOCK_POS_SET);
                if (compo == null) {
                    return List.of();
                }
                Set<ChunkPos> result = new HashSet<>();
                compo.getImmutable().forEach(bPos -> result.add(new ChunkPos(bPos)));
                return result;
            });
        }
    }


}
