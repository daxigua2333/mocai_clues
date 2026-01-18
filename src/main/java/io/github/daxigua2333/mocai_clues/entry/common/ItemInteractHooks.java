package io.github.daxigua2333.mocai_clues.entry.common;


import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.AttachedEntitySet;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.finder.ClickWithFinder;
import io.github.daxigua2333.mocai_clues.component.world.finder.FinderState;
import io.github.daxigua2333.mocai_clues.component.world.finder.FlashDotSet;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderLocation;
import io.github.daxigua2333.mocai_clues.data.client.ClientDataManager;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.server.ServerDataManager;
import io.github.daxigua2333.mocai_clues.guis.WandScreen;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.AttachingObject;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class ItemInteractHooks {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        var location = new ObjectHolderLocation<>(ObjectHolderLocation.Type.CHUNK, new BlockPosWithFace(event.getPos(), event.getFace()));
        handleWand(event,
                () -> ClientDataManager.retrieveByBlockPos(event.getPos()),
                location,
                success);

        handleFinder(event,
                () -> ServerDataManager.retrieveByBlockPos(event.getLevel(), event.getPos()),
                location,
                success);

    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        var location = new ObjectHolderLocation<>(ObjectHolderLocation.Type.ENTITY, event.getTarget().getUUID());

        handleWand(event,
                () -> ClientDataManager.retrieveByEntity(event.getTarget()),
                location,
                success);

        handleFinder(event,
                () -> ServerDataManager.retrieveByEntity(event.getTarget()),
                location,
                success);

    }

    @SubscribeEvent
    public static void onRightClickAir(PlayerInteractEvent.RightClickItem event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        handleWand(event, ClientDataManager::retrieveAllSavedData, null, success);

    }


    // ============= finder part =============
    private static void handleFinder(PlayerInteractEvent event,
                                     Supplier<List<ServerDataManager.RetrieveResult>> resultSupplier,
                                     ObjectHolderLocation location, Runnable sidedSuccess) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_FINDER_ITEM.get())) return;
        if (!event.getLevel().isClientSide()) {
            for (var res : resultSupplier.get()) {
                Consumer<ClueObject> dirty = res.markDirty();
                List<ClueObject> data = res.objects();

                for (ClueObject obj : data) {
                    // FinderState accessibility
                    FinderState bCompo = obj.getComponent(ComponentType.FINDER_STATE);
                    if (bCompo == null) throw new RuntimeException("ClueObject#" + obj.getId() + " has no FinderState component");
                    if (!bCompo.isAccessible(event.getEntity().getScoreboardName())) continue;

                    sendClue(event, obj, location);
                    // give items ...

                    bCompo.onFound();
                    dirty.accept(obj);
                }
            }
        }
        sidedSuccess.run();
    }

    private record BlockPosWithFace(BlockPos pos, Direction face) {}

    // TODO: dirty.  merge, send message callback
    private static void sendClue(PlayerInteractEvent event, ClueObject obj, ObjectHolderLocation location) {
        ClickWithFinder compo = obj.getComponent(ComponentType.SEND_CLUE);
        if (compo == null) return;

        ClueObject copy;
        if (location.data() instanceof BlockPosWithFace data) {
            // chunk
            copy = Assembler.createClueBookClue(obj, data.pos());
        } else if (location.data() instanceof UUID data) {
            // entity
            Entity e = ((ServerLevel) event.getLevel()).getEntity(data);
            copy = Assembler.createClueBookClue(obj, e);
        } else {
            throw new RuntimeException("Invalid ObjectHolderLocation.");
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();
        ObjectHolder<ClueObject> holder = player.getData(ModAttachmentRegistry.CLUE_BOOK);
        holder.put(copy);
        player.syncData(ModAttachmentRegistry.CLUE_BOOK);

    }

    // ================ wand part ==============
    private static void handleWand(PlayerInteractEvent event, Supplier<List<ClueObject>> editorSupplier, @Nullable ObjectHolderLocation location, Runnable sidedSuccess) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_WAND_ITEM.get())) return;

        switch (event.getItemStack().getOrDefault(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE)) {
            case EDITOR -> {
                if (event.getLevel().isClientSide()) {
                    WandScreen.open(
                            () -> getType(editorSupplier),
                            type -> getObjectsByType(editorSupplier, type)
                    );
                }
            }
//            case ATTACH -> caseAttach.run();
            case ATTACH -> {
                if (!event.getLevel().isClientSide() && location != null) {
                    attachManualClue(event.getEntity(), event.getItemStack(), location);
                }
            }
        }

        sidedSuccess.run();
    }


    // ======== open wand screen helpers ===========
    // TODO: optimize these 2, using AND index maybe...
    private static List<ClueType> getType(Supplier<List<ClueObject>> objects) {
        Set<ClueType> types = new HashSet<>();
        objects.get().forEach(obj -> types.add(obj.type()));
        return new ArrayList<>(types);
    }
    private static List<ClueObject> getObjectsByType(Supplier<List<ClueObject>> objects, ClueType type) {
        List<ClueObject> result = new ArrayList<>();
        for (var obj : objects.get()) {
            if (obj.type().equals(type)) {
                result.add(obj);
            }
        }
        return result;
    }


    // =========== attaching manual clue helpers ============
    private static void attachManualClue(Player player, ItemStack stack, ObjectHolderLocation location) {
        if (player == null) return;

        AttachingObject attaching = stack.get(ModDataComponentsRegistry.ATTACHING_OBJECT.get());
        if (attaching == null) {
            player.sendSystemMessage(Component.translatable("No attachment data"));
//            return InteractionResult.sidedSuccess(level.isClientSide());
            return;
        }
        // TODO: route to SD or something...
        var server = player.level().getServer();
        if (server == null) return;
        var SD = ClueObjectHolderInSavedData.getInstance(server);
        ObjectHolder<ClueObject> holder = SD.holder();
        ClueObject obj = holder.get(attaching.id());

        // add data to obj
//        switch (location.type()) {
//            case ENTITY -> {
//                var eLocation = (ObjectHolderLocation<UUID>) location;
//                AttachedEntitySet eCompo = obj.getComponentOrCreate(ComponentType.ATTACHED_ENTITY_SET, new AttachedEntitySet());
//                eCompo.add(eLocation.data());
//            }
//            case CHUNK -> {
//                var cLocation = (ObjectHolderLocation<BlockPosWithFace>) location;
//                BlockPosSet bCompo = obj.getComponentOrCreate(ComponentType.BLOCK_POS_SET, new BlockPosSet());
//                bCompo.add(cLocation.data().pos());
//
//                FlashDotSet fCompo = obj.getComponentOrCreate(ComponentType.FLASH_DOT_SET, new FlashDotSet());
//                fCompo.add(cLocation.data().pos(), cLocation.data().face());
//            }
//        }
        if (location.data() instanceof UUID data) {
            AttachedEntitySet eCompo = obj.getComponentOrCreate(ComponentType.ATTACHED_ENTITY_SET, new AttachedEntitySet());
            eCompo.add(data);
        } else if (location.data() instanceof BlockPosWithFace data) {
            BlockPosSet bCompo = obj.getComponentOrCreate(ComponentType.BLOCK_POS_SET, new BlockPosSet());
            bCompo.add(data.pos);

            FlashDotSet fCompo = obj.getComponentOrCreate(ComponentType.FLASH_DOT_SET, new FlashDotSet());
            fCompo.add(data.pos(), data.face());
        }

        // TODO: pay attention to the markDirty order
        holder.markDirty(obj);
        SD.setDirty();

        player.sendSystemMessage(Component.translatable("Attaching successfully"));
//        return InteractionResult.sidedSuccess(level.isClientSide());
    }


//    @SuppressWarnings("unchecked")
//    private static void processHolder(ObjectHolder<ClueObject> holder) {
////        Map<ClueType, Supplier<List<ClueObject>>> map = new HashMap<>(Map.of(
////                ClueType.MANUAL, ClientAccessor::retrieveAllSavedData,
////                ClueType.FOOTPRINT, List::of
////        ));
////        WandScreen.open(
////                () -> new ArrayList<>(map.keySet()),
////                (type) -> map.get(type).get()
////        );
//        var index = (ObjectHolder<ClueObject>.Index<ClueType>) holder.getIndex(ClientIndexManager.BY_CLUE_TYPE);
//        WandScreen.open(
//                () -> index == null ? List.of() : new ArrayList<>(index.keySet()),
//                (type) -> type == null || index == null ? List.of() : index.values(type)
//        );
//
//    }
}
