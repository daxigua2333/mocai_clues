package io.github.daxigua2333.mocai_clues.data.common;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosWithFace;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class IndexManager {
    public static final String BY_BLOCK_POS = "by_block_pos";
    //    public static final String BY_CHUNK_POS = "by_chunk_pos";
    public static final String BY_PASS_TYPE = "by_pass_type";
    public static final String BY_CLUE_TYPE = "by_clue_type";

    public static void ensureIndex(ObjectHolder<ClueObject> holder, String by) {
//        egs:
//        if (holder.getIndex().isEmpty()) {
//            holder.createUniqueIndex();
//        }

        if (holder.getIndex(by) == null) {  // ensure
            switch (by) {
                case BY_BLOCK_POS -> holder.createIndex(BY_BLOCK_POS, obj -> {
                    BlockPosWithFace compo = obj.getComponent(ComponentType.BLOCK_POS_WITH_FACE);
                    if (compo == null) {
                        return null;
                    }
                    return compo.getPos();
                });
//                case BY_BLOCK_POS -> holder.createMultiIndex(BY_BLOCK_POS, obj -> {
//                    BlockPosSet compo = obj.getComponent(ComponentType.BLOCK_POS_SET);
//                    if (compo == null) {
//                        return List.of();
//                    }
//                    return compo.getImmutable();
//                });
//                case BY_CHUNK_POS -> holder.createMultiIndex(BY_CHUNK_POS, obj -> {
//                    BlockPosSet compo = obj.getComponent(ComponentType.BLOCK_POS_SET);
//                    if (compo == null) {
//                        return List.of();
//                    }
//                    Set<ChunkPos> result = new HashSet<>();
//                    compo.getImmutable().forEach(bPos -> result.add(new ChunkPos(bPos)));
//                    return result;
//                });
                case BY_PASS_TYPE -> holder.createMultiIndex(BY_PASS_TYPE, obj -> {
                    Set<PassType> result = new HashSet<>();
                    RendererHolder rCompo = obj.getComponent(ComponentType.RENDERER_HOLDER);
                    if (rCompo == null) return List.of();
                    rCompo.getImmutable().forEach(e -> result.add(e.getPassType()));
                    return result;
                });
                case BY_CLUE_TYPE -> holder.createIndex(BY_CLUE_TYPE, ClueObject::type);
                case null, default -> throw new RuntimeException("Unimplemented index initialization.");
            }
        }
    }

    /**
     * client index schema(whenever a holder is newed): when init syncing
     */
    public static class Client {
        private static void commonEnsure(ObjectHolder<ClueObject> holder) {
            ensureIndex(holder, BY_BLOCK_POS);
            ensureIndex(holder, BY_CLUE_TYPE);
        }

        // TODO: 2 unsafe type declare... but if there is nothing other than ClueObject, it is safe
        @SuppressWarnings("unchecked")
        public static <T> void attachmentHolderEnsure(ObjectHolder<T> tHolder) {
            ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
            commonEnsure(holder);

            ensureIndex(holder, BY_PASS_TYPE);
        }

        @SuppressWarnings("unchecked")
        public static <T> void savedDataEnsure(ObjectHolder<T> tHolder) {
            ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
            commonEnsure(holder);

//            ensureIndex(holder, BY_CHUNK_POS);
        }
    }

    public static class Server {
        private static void commonEnsure(ObjectHolder<ClueObject> holder) {
            ensureIndex(holder, BY_BLOCK_POS);
        }

        @SuppressWarnings("unchecked")
        public static <T> void attachmentHolderEnsure(ObjectHolder<T> tHolder) {
            ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
            commonEnsure(holder);
        }

        @SuppressWarnings("unchecked")
        public static <T> void savedDataEnsure(ObjectHolder<T> tHolder) {
            ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
            commonEnsure(holder);
        }

    }

    @SuppressWarnings("unchecked")
    public static List<ClueObject> byBlockPos(ObjectHolder<ClueObject> holder, BlockPos pos) {
        var index = (ObjectHolder<ClueObject>.Index<BlockPos>) holder.getIndex(BY_BLOCK_POS);
        if (index != null) {
            return index.values(pos);
        }
        return List.of();
    }

//    @SuppressWarnings("unchecked")
//    public static List<ClueObject> byChunkPos(ObjectHolder<ClueObject> holder, ChunkPos chunkPos) {
//        var index = (ObjectHolder<ClueObject>.Index<ChunkPos>) holder.getIndex(BY_CHUNK_POS);
//        if (index != null) {
//            return index.values(chunkPos);
//        }
//        return List.of();
//    }

//    @SuppressWarnings("unchecked")
//    public static List<ClueObject> passType(ObjectHolder<ClueObject> holder, )
}
