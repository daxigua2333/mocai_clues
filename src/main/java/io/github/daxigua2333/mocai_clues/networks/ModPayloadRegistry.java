package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.system.discovery.InteractResult;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderLocation;
import io.github.daxigua2333.mocai_clues.data.server.ServerDataManager;
import io.github.daxigua2333.mocai_clues.guis.widget.DetailPanelInClueBook;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.AttachingObject;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
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

@EventBusSubscriber(modid = MoCaiClues.MODID)
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


        // ====== ClueObject =====
        registrar.playToServer(
                ClueObjectUpdatePayload.TYPE,
                ClueObjectUpdatePayload.STREAM_CODEC,
                (final ClueObjectUpdatePayload payload, final IPayloadContext context) -> {
                    context.enqueueWork(() -> {
//                        MyObjectSync.server().store().serverUpsert(payload.object());
//                        ServerDataAccessor.upsert(context.player().level().getServer(), payload.object());
                        switch (payload.data().mode()) {
                            case DELETE -> {
                                switch (payload.location().type()) {
                                    case SD ->
                                            ServerDataManager.delete(context.player().getServer(), payload.data().id());
                                    case CHUNK ->
                                            ServerDataManager.delete((ServerLevel) context.player().level(), payload.location().chunkPos(), payload.data().id());
                                    case ENTITY ->
                                            ServerDataManager.delete((ServerLevel) context.player().level(), payload.location().entityId(), payload.data().id());
                                }
                            }
                            case UPSERT -> {
                                switch (payload.location().type()) {
                                    case SD ->
                                            ServerDataManager.upsert(context.player().getServer(), payload.data().obj());
                                    case CHUNK ->
                                            ServerDataManager.upsert((ServerLevel) context.player().level(), payload.location().chunkPos(), payload.data().obj());
                                    case ENTITY ->
                                            ServerDataManager.upsert((ServerLevel) context.player().level(), payload.location().entityId(), payload.data().obj());
                                }
                            }
                        }
                    });
                }
        );


        // ===== manual =====
        // client create new one
        registrar.playToServer(
                ManualClueCreatePayload.TYPE,
                ManualClueCreatePayload.STREAM_CODEC,
                (final ManualClueCreatePayload payload, final IPayloadContext context) -> {
                    context.enqueueWork(() -> {
                        ServerDataManager.upsert(context.player().getServer(), Assembler.createManualClue());
                    });
                }
        );
        registrar.playToServer(
                WandSwitchToAttachModePayload.TYPE,
                WandSwitchToAttachModePayload.STREAM_CODEC,
                (final WandSwitchToAttachModePayload payload, final IPayloadContext context) -> {
                    context.enqueueWork(() -> {
                        // update Database
                        ServerDataManager.upsert(context.player().level().getServer(), payload.object());
                        // update main hand
                        Player player = context.player();
                        ItemStack stack = player.getMainHandItem();
                        if (stack.is(ModItemsRegistry.CLUE_WAND_ITEM.get())) {
                            // update mode
                            stack.update(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE, current -> WandMode.ATTACH);
                            player.displayClientMessage(Component.literal("Wand mode: " + WandMode.ATTACH.toString()), true);
                            // update attaching
                            stack.update(ModDataComponentsRegistry.ATTACHING_OBJECT.get(),
                                    new AttachingObject(payload.object().getId()), current -> new AttachingObject(payload.object().getId()));
                        } else {
                            throw new RuntimeException("Why you are not holding the wand???");
                        }

                    });
                }
        );

        // player share
        registrar.playToServer(
                DetailPanelInClueBook.ShareCluePayload.TYPE,
                DetailPanelInClueBook.ShareCluePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer from = (ServerPlayer) context.player();
                    if (from.getServer() == null) return;
                    ServerPlayer to = from.getServer().getPlayerList().getPlayer(payload.playerId());
                    InteractResult.sendClue(to, payload.obj(), new ObjectHolderLocation<>(ObjectHolderLocation.Type.ENTITY, from));
                })
        );
    }


    private static void onWandLeftButtonPressed(final LeftButtonPressedPayload data, final IPayloadContext context) {
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

}