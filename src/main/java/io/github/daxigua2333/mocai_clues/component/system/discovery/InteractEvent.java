//package io.github.daxigua2333.mocai_clues.component.system.discovery;
//
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.component.ComponentType;
//import io.github.daxigua2333.mocai_clues.component.world.finder.FinderState;
//import io.github.daxigua2333.mocai_clues.data.ObjectHolderLocation;
//import io.github.daxigua2333.mocai_clues.data.common.DataManager;
//import io.github.daxigua2333.mocai_clues.data.ObjectsWithLocation;
//import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
//import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
//import net.minecraft.core.BlockPos;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.player.Player;
//import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.function.Supplier;
//
//public final class InteractEvent {
//
//    // ============= finder part =============
//    public static void clickWithFinder(PlayerInteractEvent event,
//                                       Supplier<List<ObjectsWithLocation>> resultSupplier,
//                                       ObjectHolderLocation locationData, Runnable sidedSuccess) {
//        if (!event.getItemStack().is(ModItemsRegistry.CLUE_FINDER_ITEM.get())) return;
//        if (!event.getLevel().isClientSide()) {
//            for (var res : resultSupplier.get()) {
//                IRuntimeLocation location = res.location();
//                List<ClueObject> data = res.objects();
//
//                for (ClueObject obj : data) {
//                    // do have active behavior check
//                    if (!obj.hasComponent(ComponentType.CLICK_WITH_FINDER)) continue;
//                    // FinderState accessibility check
//                    FinderState bCompo = obj.getComponent(ComponentType.FINDER_STATE);
//                    if (bCompo == null)
//                        throw new RuntimeException("ClueObject#" + obj.getId() + " has no FinderState component");
//                    if (!bCompo.isAccessible(event.getEntity().getScoreboardName())) continue;
//
//                    // TODO: .....the same as the next methods, so reuse
//                    InteractResult.sendClue((ServerPlayer) event.getEntity(), obj, locationData);
//                    InteractResult.sendItem();
//
//                    bCompo.onFound();
//                    location.markDirty(obj);
//                }
//            }
//        }
//        sidedSuccess.run();
//    }
//
//    private final static Map<UUID, BlockPos> prevPos = new HashMap<>();
//
//    public static void walkOn(Player player, BlockPos pos) {
//        if (player.level().isClientSide()) return;
//        if (pos.equals(prevPos.get(player.getUUID()))) return;
//        prevPos.put(player.getUUID(), pos);
//        //        if (!entity.isAlive()) return;
////        if (!entity.onGround()) return;
////        if (!entity.verticalCollisionBelow) return;  // make sure collide with ground
////        if (entityIsMoving(entity)) {  // Only when actually moving horizontally
////            return;
////        }
//
//        ObjectsWithLocation r = DataManager.Server.retrieveByBlockPos(player.level(), pos);
//        IRuntimeLocation location = r.location();
//        List<ClueObject> data = r.objects();
//
//        for (ClueObject obj : data) {
//            // do have behavior check
//            if (!obj.hasComponent(ComponentType.WALK_ON)) continue;
//            // FinderState accessibility check
//            FinderState bCompo = obj.getComponent(ComponentType.FINDER_STATE);
//            if (bCompo == null)
//                throw new RuntimeException("ClueObject#" + obj.getId() + " has no FinderState component");
//            if (!bCompo.isAccessible(player.getScoreboardName())) continue;
//
//            InteractResult.sendClue((ServerPlayer) player, obj, new ObjectHolderLocation<>(ObjectHolderLocation.Type.CHUNK, pos));
//            InteractResult.sendItem();
//
//            bCompo.onFound();
//            location.markDirty(obj);
//        }
//    }
//
////    private static void handleObjects(List<ClueObject> data, ComponentType eventType, Runnable handleResult) {
////        for (var obj : data) {
////            // do have behavior check
////            if (!obj.hasComponent(eventType)) continue;
////            // FinderState accessibility check
////            FinderState bCompo = obj.getComponent(ComponentType.FINDER_STATE);
////            if (bCompo == null)
////                throw new RuntimeException("ClueObject#" + obj.getId() + " has no FinderState component");
////            if (!bCompo.isAccessible(player.getScoreboardName())) continue;
////
////            InteractResult.sendClue(player, obj, new ObjectHolderLocation<>(ObjectHolderLocation.Type.CHUNK, pos));
////            dirty.accept(obj);
////
////            bCompo.onFound();
////
////        }
////    }
//}
