package io.github.daxigua2333.mocai_clues.entry.client;


import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.AttachedEntitySet;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.finder.ClickWithFinder;
import io.github.daxigua2333.mocai_clues.component.world.finder.FlashDotSet;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.client.api.ClientAccessor;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.server.api.ServerDataAccessor;
import io.github.daxigua2333.mocai_clues.guis.WandScreen;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.AttachingObject;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class ItemInteractHooks {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        handleWand(event, () -> ClientAccessor.retrieveByBlockPos(event.getPos()), () -> {
            if (!event.getLevel().isClientSide()) {
                attachManualClue(event.getEntity(), event.getItemStack(),
                        obj -> addPos(obj, event.getPos(), event.getFace())
                );
            }
        }, success);

        handleFinder(event, () -> ServerDataAccessor.retrieveByBlockPos(event.getLevel(), event.getPos()), obj -> {
            ClickWithFinder compo = obj.getComponent(ComponentType.SEND_CLUE);
            if (compo == null) return;
            compo.send((ServerPlayer) event.getEntity(),
                    old -> Assembler.createClueBookClue(old, event.getPos()));
        }, success);

    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        handleWand(event, () -> ClientAccessor.retrieveByEntity(event.getTarget()), () -> {
            if (!event.getLevel().isClientSide()) {
                attachManualClue(event.getEntity(), event.getItemStack(),
                        obj -> addEntityUUID(obj, event.getTarget().getUUID())
                );
            }
        }, success);

        handleFinder(event, () -> ServerDataAccessor.retrieveByEntity(event.getTarget()), obj -> {
            ClickWithFinder compo = obj.getComponent(ComponentType.SEND_CLUE);
            if (compo == null) return;
            compo.send((ServerPlayer) event.getEntity(),
                    old -> Assembler.createClueBookClue(old, event.getTarget()));
        }, success);

    }

    @SubscribeEvent
    public static void onRightClickAir(PlayerInteractEvent.RightClickItem event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        handleWand(event, ClientAccessor::retrieveAllSavedData, () -> {}, success);

    }


    private static void handleFinder(PlayerInteractEvent event, Supplier<List<ClueObject>> dataSupplier, Consumer<ClueObject> objHandler, Runnable sidedSuccess) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_FINDER_ITEM.get())) return;
        if (!event.getLevel().isClientSide()) {
            List<ClueObject> data = dataSupplier.get();
            for (ClueObject obj : data) {
                objHandler.accept(obj);
            }
        }
        sidedSuccess.run();
    }


    private static void handleWand(PlayerInteractEvent event, Supplier<List<ClueObject>> editorSupplier, Runnable caseAttach, Runnable sidedSuccess) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_WAND_ITEM.get())) return;

        switch (event.getItemStack().getOrDefault(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE)) {
            case EDITOR -> {
                if (event.getLevel().isClientSide()) {
                    openWandScreen(editorSupplier);
                }
            }
            case ATTACH -> caseAttach.run();
        }

        sidedSuccess.run();
    }


    // ======== open wand screen helpers ===========
    private static void openWandScreen(Supplier<List<ClueObject>> objects) {
        WandScreen.open(
                () -> getType(objects),
                type -> getObjectsByType(objects, type)
        );
    }
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
    private static void attachManualClue(Player player, ItemStack stack, Consumer<ClueObject> addData) {
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

        addData.accept(obj);

        // TODO: pay attention to the markDirty order
        holder.markDirty(obj);
        SD.setDirty();

        player.sendSystemMessage(Component.translatable("Attaching successfully"));
//        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void addPos(ClueObject obj, BlockPos pos, Direction face) {
        BlockPosSet bCompo = obj.getComponentOrCreate(ComponentType.BLOCK_POS_SET, new BlockPosSet());
        bCompo.add(pos);

        FlashDotSet fCompo = obj.getComponentOrCreate(ComponentType.FLASH_DOT_SET, new FlashDotSet());
        fCompo.add(pos, face);
    }

    private static void addEntityUUID(ClueObject obj, UUID id) {
        AttachedEntitySet eCompo = obj.getComponentOrCreate(ComponentType.ATTACHED_ENTITY_SET, new AttachedEntitySet());
        eCompo.add(id);
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
