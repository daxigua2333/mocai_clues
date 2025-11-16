package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.items.ClueFinderItem;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.FinderHitResult;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.statics.FinderHitResultTicker;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

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
        ItemStack stack;
        if (data.isMain()) {
            stack = player.getMainHandItem();
        }else {
            stack = player.getOffhandItem();
        }
//        var prev = FinderHitResultTicker.prevMainHoldFinderDataRecord.getOrDefault(player.getUUID(), null);
//        var prev = FinderHitResultTicker.prevMainHoldFinderPatchedDataComponentMap.getOrDefault(player.getUUID(), null);
        ItemStack prev = FinderHitResultTicker.prevMainHoldFinderMap.getOrDefault(player.getUUID(), ItemStack.EMPTY);
        List<ItemStack> copies = FinderHitResultTicker.prevMainHoldFinderCopyMap.getOrDefault(prev, new ArrayList<>());
        MoCaiClues.LOGGER.debug("Payload handler: prev data:{}", prev);
        MoCaiClues.LOGGER.debug("Payload handler: prev copies:{}", copies);
        MoCaiClues.LOGGER.debug("Payload handler: full copies:{}", FinderHitResultTicker.prevMainHoldFinderCopyMap);

        var finder = ModItemsRegistry.CLUE_FINDER_ITEM.get();
        if (prev.getItem() == finder) {
            FinderHitResultTicker.handleHitResult(player, prev, false);
        }
        for (ItemStack stackI : copies) {
            if (stackI.getItem() == finder) {
                FinderHitResultTicker.handleHitResult(player, stackI, false);
            }
        }
//        if (prev != null) {
//            prev = new FinderHitResult(prev.current(), false);
//            var type = ModDataComponentsRegistry.FINDER_HIT_RESULT.get();
//            prev.set(type, new FinderHitResult(prev.getOrDefault(type, new FinderHitResult(false, false)).current(), false));
//            MoCaiClues.LOGGER.debug("Payload handler: current data:{}", prev);
//        }
//        FinderHitResultTicker.handleHitResult(player, stack, false);
    }
}