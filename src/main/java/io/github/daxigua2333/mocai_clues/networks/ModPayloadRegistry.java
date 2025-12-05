package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.network.BaseSyncHandler;
import io.github.daxigua2333.mocai_clues.data.client.ClientClueObjectMainMapSavedData;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectMainMapInSavedData;
import io.github.daxigua2333.mocai_clues.data.server.api.SavedDataCreator;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.statics.FinderHitResultTicker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.*;

@EventBusSubscriber(modid=MoCaiClues.MODID)
public class ModPayloadRegistry {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        MoCaiClues.LOGGER.info("Registering ModPayloadRegistry");
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN);

        registrar.playToServer(
                LeftButtonPressedPayload.TYPE,
                LeftButtonPressedPayload.STREAM_CODEC,
                ModPayloadRegistry::onWandLeftButtonPressed
        );

        registrar.playToServer(
                FinderLeaveHandPayload.TYPE,
                FinderLeaveHandPayload.STREAM_CODEC,
                ModPayloadRegistry::onFinderLeaveHand
        );


        // ====== ClueObject =====
        registrar.playToServer(
                ClueObjectHolderInitialSyncPayload.TYPE,
                ClueObjectHolderInitialSyncPayload.STREAM_CODEC,
                (final ClueObjectHolderInitialSyncPayload payload, final IPayloadContext context) -> {
                    context.enqueueWork(() -> {
                        ClueObject object = payload.object();
                        // iterate all SyncHandler, invoke handleSync
                        for (ClueComponent component : object.getComponents()) {
                            if (component instanceof BaseSyncHandler) {
                                ((BaseSyncHandler) component).handleSync((ServerLevel) context.player().level());
                            }
                        }
                    });
                }
        );

        // ===== manual =====
        // client initial sync request
        registrar.playToServer(
                ClientActivelySyncSavedDataPayload.TYPE,
                ClientActivelySyncSavedDataPayload.STREAM_CODEC,
                (final ClientActivelySyncSavedDataPayload payload, final IPayloadContext context) -> {
                    context.enqueueWork(() -> {
                        ServerPlayer player = (ServerPlayer) context.player();
                        ClueObjectMainMapInSavedData.getInstance(player.serverLevel()).syncAllToOne(player);
                        // TODO: test
                        ClueObjectMainMapInSavedData.getInstance(player.serverLevel()).clear();
                        // maybe other SavedData syncs
                    });
                }
        );
        // client create new one
        registrar.playToServer(
                ManualClueCreatePayload.TYPE,
                ManualClueCreatePayload.STREAM_CODEC,
                (final ManualClueCreatePayload payload, final IPayloadContext context) -> {
                    context.enqueueWork(() -> {
                        SavedDataCreator.createDefault(ClueType.MANUAL, (ServerLevel) context.player().level());
                    });
                }
        );
        // server initial sync
        registrar.playToClient(
                ClueObjectMainMapSyncPayload.TYPE,
                ClueObjectMainMapSyncPayload.STREAM_CODEC,
                (final ClueObjectMainMapSyncPayload payload, final IPayloadContext context) -> {
                    context.enqueueWork(() -> {
                        ClientClueObjectMainMapSavedData.getInstance().setMap(payload.map());
                    });
                }
        );
        // server sync delta
        registrar.playToClient(
                ClueObjectHolderDeltaSyncPayload.TYPE,
                ClueObjectHolderDeltaSyncPayload.STREAM_CODEC,
                (final ClueObjectHolderDeltaSyncPayload payload, final IPayloadContext context) -> {
                    context.enqueueWork(() -> {
                        ClientClueObjectMainMapSavedData.getInstance().addToDeltas(payload);
                        ClientClueObjectMainMapSavedData.getInstance().applyDelta(payload);
                    });
                }
        );

    }


    private static void onWandLeftButtonPressed(final LeftButtonPressedPayload data, final IPayloadContext context){
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() == ModItemsRegistry.CLUE_WAND_ITEM.get()) {
                // update(...) will set the component (creates it if needed) and returns the old/updated value
                stack.update(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE, current -> {
                    // If the component was missing current could be null - guard
                    WandMode cur = current == null ? WandMode.CREATE : current;
                    return cur.next();
                });
                WandMode newMode = stack.getOrDefault(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE);
                // show simple chat feedback (server -> client message to the player)
                player.displayClientMessage(Component.literal("Wand mode: " + newMode.toString()), true);
            }
        })
        .exceptionally(e -> {
            // Handle exception
            context.disconnect(Component.translatable("my_mod.networking.failed", e.getMessage()));
            return null;
        });
    }

    private static void onFinderLeaveHand(final FinderLeaveHandPayload data, final IPayloadContext context) {
        Player player = context.player();
        ItemStack prev;
        List<ItemStack> copies;
        if (data.isMain()) {
            prev = FinderHitResultTicker.prevMainHoldFinderMap.getOrDefault(player.getUUID(), ItemStack.EMPTY);
            copies = FinderHitResultTicker.prevMainHoldFinderCopyMap.getOrDefault(prev, new ArrayList<>());
        }else {
            prev = FinderHitResultTicker.prevOffHoldFinderMap.getOrDefault(player.getUUID(), ItemStack.EMPTY);
            copies = FinderHitResultTicker.prevOffHoldFinderCopyMap.getOrDefault(prev, new ArrayList<>());
        }

        // turn prev + copies all into false
        var finder = ModItemsRegistry.CLUE_FINDER_ITEM.get();
        if (prev.getItem() == finder) {
            FinderHitResultTicker.handleHitResult(player, prev, false);
        }
        for (ItemStack stackI : copies) {
            if (stackI.getItem() == finder) {
                FinderHitResultTicker.handleHitResult(player, stackI, false);
            }
        }
    }
}