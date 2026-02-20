package io.github.daxigua2333.mocai_clues.entry.client;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.data.BlockPosWithFace;
import io.github.daxigua2333.mocai_clues.data.ObjectsWithLocation;
import io.github.daxigua2333.mocai_clues.data.common.DataManager;
import io.github.daxigua2333.mocai_clues.data.location.factory.FromSavedDataSerializable;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;
import io.github.daxigua2333.mocai_clues.guis.AttachedClueEditorScreen;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectUpdatePayload;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public final class WandHooks {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_WAND_ITEM.get())) return;

        handleWand(event.getItemStack(), event.getLevel(),
                () -> DataManager.Common.retrieveByBlockPos(event.getLevel(), event.getPos()),
                new BlockPosWithFace(event.getPos(), event.getFace()));

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.getItemStack().is(ModItemsRegistry.CLUE_WAND_ITEM.get())) return;

        handleWand(event.getItemStack(), event.getLevel(),
                () -> DataManager.Client.retrieveByEntity(event.getTarget()),
                new BlockPosWithFace());

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
    }

    @SubscribeEvent
    public static void onRightClickAir(PlayerInteractEvent.RightClickItem event) {
    }


    private static void handleWand(ItemStack wand, Level level,
                                   Supplier<ObjectsWithLocation> editorSupplier, BlockPosWithFace posWithFace) {

        switch (wand.getOrDefault(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.values()[0])) {
            case EDITOR -> {
                if (level.isClientSide()) {
                    ObjectsWithLocation ol = editorSupplier.get();
                    ISerializableLocation location = ol.location().getSerializable();
                    if (location instanceof FromSavedDataSerializable) {
                        throw new RuntimeException("Unimplemented.");
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
                                )),
                                location,
                                posWithFace
                        );
                    }

                }
            }
            default -> {
            }
        }
    }


    private static List<ClueType> getType(Supplier<ObjectsWithLocation> data) {
        EnumSet<ClueType> types = EnumSet.noneOf(ClueType.class);
        types.add(ClueType.MANUAL);
        types.add(ClueType.ITEM);
        data.get().objects().forEach(obj -> types.add(obj.type()));
        return new ArrayList<>(types);
    }

    private static List<ClueObject> getObjectsByType(Supplier<ObjectsWithLocation> data, ClueType type) {
        List<ClueObject> result = new ArrayList<>();
        for (var obj : data.get().objects()) {
            if (obj.type().equals(type)) {
                result.add(obj);
            }
        }
        return result;
    }

}
