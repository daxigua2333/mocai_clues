package io.github.daxigua2333.mocai_clues.entry.common;


import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.system.discovery.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.data.AttachedEntitySet;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.finder.FlashDotSet;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class ItemInteractHooks {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        var location = new ObjectHolderLocation<>(ObjectHolderLocation.Type.CHUNK, new ObjectHolderLocation.BlockPosWithFace(event.getPos(), event.getFace()));
        handleWand(event,
                () -> ClientDataManager.retrieveByBlockPos(event.getPos()),
                location,
                success);

        InteractEvent.clickWithFinder(event,
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

        InteractEvent.clickWithFinder(event,
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
        } else if (location.data() instanceof ObjectHolderLocation.BlockPosWithFace data) {
            BlockPosSet bCompo = obj.getComponentOrCreate(ComponentType.BLOCK_POS_SET, new BlockPosSet());
            bCompo.add(data.pos());

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
