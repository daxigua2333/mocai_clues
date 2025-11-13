package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.items.ClueFinderItem;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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

        registrar.playToClient(
                FinderHitResultPayload.TYPE,
                FinderHitResultPayload.STREAM_CODEC,
                ClueFinderItem::handleHitResult
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
}