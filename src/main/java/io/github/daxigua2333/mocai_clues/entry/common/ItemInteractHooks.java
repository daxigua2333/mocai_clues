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
import io.github.daxigua2333.mocai_clues.data.common.DataManager;
import io.github.daxigua2333.mocai_clues.data.common.RetrieveResult;
import io.github.daxigua2333.mocai_clues.data.location.factory.FromChunkAttachmentSerializable;
import io.github.daxigua2333.mocai_clues.data.location.factory.FromEntityAttachmentSerializable;
import io.github.daxigua2333.mocai_clues.data.location.factory.FromSavedDataSerializable;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.server.ServerDataManager;
import io.github.daxigua2333.mocai_clues.guis.AttachedClueEditorScreen;
import io.github.daxigua2333.mocai_clues.guis.ManualClueEditorScreen;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.AttachingObject;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectUpdatePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

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

        handleWand(event,
                () -> DataManager.Common.retrieveByBlockPos(event.getLevel(), event.getPos()),
                new FromChunkAttachmentSerializable(new ChunkPos(event.getPos())),
                success);

        InteractEvent.clickWithFinder(event,
                () -> ServerDataManager.retrieveByBlockPos(event.getLevel(), event.getPos()),
                new ObjectHolderLocation<>(ObjectHolderLocation.Type.CHUNK, new ObjectHolderLocation.BlockPosWithFace(event.getPos(), event.getFace())),
                success);

    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        handleWand(event,
                () -> DataManager.Common.retrieveByEntity(event.getTarget()),
                new FromEntityAttachmentSerializable(event.getTarget().getUUID()),
                success);

        InteractEvent.clickWithFinder(event,
                () -> ServerDataManager.retrieveByEntity(event.getTarget()),
                new ObjectHolderLocation<>(ObjectHolderLocation.Type.ENTITY, event.getTarget().getUUID()),
                success);

    }

    @SubscribeEvent
    public static void onRightClickAir(PlayerInteractEvent.RightClickItem event) {
        Runnable success = () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        };

        handleWand(event, ClientDataManager::retrieveAllSavedData, new FromSavedDataSerializable(), success);

    }


    // ================ wand part ==============
    private static void handleWand(PlayerInteractEvent event, Supplier<List<RetrieveResult>> editorSupplier, Runnable sidedSuccess) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_WAND_ITEM.get())) return;

        switch (event.getItemStack().getOrDefault(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE)) {
            case EDITOR -> {
                if (event.getLevel().isClientSide()) {
                    openScreen(editorSupplier, location);
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
    private static void openScreen(Supplier<List<ClueObject>> editorSupplier, ISerializableLocation location) {
        if (location instanceof FromSavedDataSerializable) {
            // manual
            ManualClueEditorScreen.open(
                    editorSupplier,
                    obj -> PacketDistributor.sendToServer(new ClueObjectUpdatePayload(
                            location,
                            new ClueObjectUpdatePayload.Data(obj)
                    )),
                    obj -> PacketDistributor.sendToServer(new ClueObjectUpdatePayload(
                            location,
                            new ClueObjectUpdatePayload.Data(obj.getId())
                    ))
            );
        } else {
            // attachment
            AttachedClueEditorScreen.open(
                    () -> getType(editorSupplier),
                    type -> getObjectsByType(editorSupplier, type),
                    obj -> PacketDistributor.sendToServer(new ClueObjectUpdatePayload(
                            location,
                            new ClueObjectUpdatePayload.Data(obj)
                    )),
                    obj -> PacketDistributor.sendToServer(new ClueObjectUpdatePayload(
                            location,
                            new ClueObjectUpdatePayload.Data(obj.getId())
                    ))
            );


        }
    }

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
