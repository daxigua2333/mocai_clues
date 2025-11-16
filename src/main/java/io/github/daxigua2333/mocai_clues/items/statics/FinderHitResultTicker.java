package io.github.daxigua2333.mocai_clues.items.statics;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainerAttachmentHelper;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.FinderHitResult;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class FinderHitResultTicker {

    public static final Map<UUID, ItemStack> prevMainHoldFinderMap = new ConcurrentHashMap<>();
    public static final Map<ItemStack, List<ItemStack>> prevMainHoldFinderCopyMap = new IdentityHashMap<>();
//    private static final Map<UUID, ItemStack> prevOffHoldMap = new ConcurrentHashMap<>();
//    public static final Map<UUID, FinderHitResult> prevMainHoldFinderDataRecord = new ConcurrentHashMap<>();
//    public static final Map<UUID, PatchedDataComponentMap> prevMainHoldFinderPatchedDataComponentMap = new ConcurrentHashMap<>();



    // ticker
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) throws NoSuchFieldException, IllegalAccessException {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        Item finder = ModItemsRegistry.CLUE_FINDER_ITEM.get();
        // if neither hand holds finder
//        ItemStack prevMainHold = prevMainHoldMap.getOrDefault(player.getUUID(), ItemStack.EMPTY);
//        ItemStack prevOffHold = prevOffHoldMap.getOrDefault(player.getUUID(), ItemStack.EMPTY);
//        if (prevMainHold.getItem() == finder && main.getItem() != finder) {handleHitResult(player, prevMainHold, false);}
//        if (prevOffHold.getItem() == finder && off.getItem() != finder) {handleHitResult(player, prevOffHold, false);}
//        if(prevMainHold.getItem() != main.getItem()) {MoCaiClues.LOGGER.debug("{} -> {}", prevMainHold, main);}
//        prevMainHoldMap.put(player.getUUID(), main);
//        prevOffHoldMap.put(player.getUUID(), off);
        boolean currentDoHold = main.getItem() == finder || off.getItem() == finder;
        if (!currentDoHold) {return;}
//        if (main.getItem() == finder) {prevMainHoldFinderDataRecord.put(player.getUUID(), main.getOrDefault(ModDataComponentsRegistry.FINDER_HIT_RESULT.get(), new FinderHitResult(false, false)));}
        if (main.getItem() == finder) {
//            Field field = main.getClass().getDeclaredField("components");
//            field.setAccessible(true);
//            PatchedDataComponentMap map = (PatchedDataComponentMap) field.get(main);
//            prevMainHoldFinderPatchedDataComponentMap.put(player.getUUID(), map);
            prevMainHoldFinderMap.put(player.getUUID(), main);
            prevMainHoldFinderCopyMap.clear();
        }

        // raytrace: player.pick(range, partialTicks, ClipContext.Fluid.NONE)
        HitResult hr = player.pick(Config.FINDER_HIT_DISTANCE.get(), 0.0F, false);  // TODO: config
        if (hr.getType() != HitResult.Type.BLOCK) return;

        // optional: check which face was hit: bhr.getDirection()

        BlockHitResult bhr = (BlockHitResult) hr;
        BlockPos pos = bhr.getBlockPos();
        // notify client (change data and play sound)
        if (ClueContainerAttachmentHelper.containsKey(player.level(), pos)) {
            if (main.getItem() == finder) {handleHitResult(player, main, true);}
            if (off.getItem() == finder) {handleHitResult(player, off, true);}
        }else{
            if (main.getItem() == finder) {handleHitResult(player, main, false);}
            if (off.getItem() == finder) {handleHitResult(player, off, false);}
        }
    }

    public static void handleHitResult(Player player, ItemStack stack, boolean success) {
        if (stack == ItemStack.EMPTY) return;

        var type = ModDataComponentsRegistry.FINDER_HIT_RESULT.get();
        FinderHitResult result = stack.getOrDefault(type, new FinderHitResult(false, false));
        var newResult = new FinderHitResult(result.current(), success);
        stack.set(type, newResult);

        if (!newResult.prev() && newResult.current()) {
            player.level().playSound(null, player.getOnPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1f, 1f);
        }
    }


}
