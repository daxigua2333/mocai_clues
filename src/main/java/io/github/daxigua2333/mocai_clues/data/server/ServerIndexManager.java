//package io.github.daxigua2333.mocai_clues.data.server;
//
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.component.ComponentType;
//import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
//import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
//
//import java.util.List;
//
//@Deprecated
//public final class ServerIndexManager {
//    public static final String BY_BLOCK_POS = "by_block_pos";
//
//    private static void commonEnsure(ObjectHolder<ClueObject> holder) {
//        if (holder.getIndex(BY_BLOCK_POS) == null) {
//            holder.createMultiIndex(BY_BLOCK_POS, obj -> {
//                BlockPosSet compo = obj.getComponent(ComponentType.BLOCK_POS_SET);
//                if (compo == null) {
//                    return List.of();
//                }
//                return compo.getImmutable();
//            });
//        }
//
//    }
//
//    // TODO: 2 unsafe type declare... but if there is nothing other than ClueObject, it is safe
//    @SuppressWarnings("unchecked")
//    public static <T> void attachmentHolderEnsure(ObjectHolder<T> tHolder) {
//        ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
//        commonEnsure(holder);
//
//    }
//
//    @SuppressWarnings("unchecked")
//    public static <T> void savedDataEnsure(ObjectHolder<T> tHolder) {
//        ObjectHolder<ClueObject> holder = (ObjectHolder<ClueObject>) tHolder;
//        commonEnsure(holder);
//
//    }
//
//}
