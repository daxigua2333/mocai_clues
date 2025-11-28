package io.github.daxigua2333.mocai_clues.items.statics;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.data_attachments.statics.ClueContainerAttachmentHelper;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.FinderHitResult;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber
public class FinderHitResultTicker {

    public static final Map<UUID, ItemStack> prevMainHoldFinderMap = new ConcurrentHashMap<>();
    public static final Map<ItemStack, List<ItemStack>> prevMainHoldFinderCopyMap = new IdentityHashMap<>();
    public static final Map<UUID, ItemStack> prevOffHoldFinderMap = new ConcurrentHashMap<>();
    public static final Map<ItemStack, List<ItemStack>> prevOffHoldFinderCopyMap = new IdentityHashMap<>();


    // ticker
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        Item finder = ModItemsRegistry.CLUE_FINDER_ITEM.get();
        // if neither hand holds finder
        boolean currentDoHold = main.getItem() == finder || off.getItem() == finder;
        if (!currentDoHold) {return;}
        // leave hand data
        if (main.getItem() == finder) {
            prevMainHoldFinderMap.put(player.getUUID(), main);
            prevMainHoldFinderCopyMap.clear();
        }
        if (off.getItem() == finder) {
            prevOffHoldFinderMap.put(player.getUUID(), main);
            prevOffHoldFinderCopyMap.clear();
        }

        // raytrace: player.pick(range, partialTicks, ClipContext.Fluid.NONE)
        HitResult hr = player.pick(Config.COMMON.FINDER_HIT_DISTANCE.get(), 0.0F, false);  // TODO: config
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
//            player.level().playSound(null, player.getOnPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1f, 1f);
            // TODO: test
            var pos = player.getOnPos();
            var registry = player.level().registryAccess().registryOrThrow(Registries.SOUND_EVENT);
            var holder = registry.getResourceKey(SoundEvents.EXPERIENCE_ORB_PICKUP).flatMap(registry::getHolder).orElseThrow(() -> new IllegalArgumentException("Sound not registered"));
            ClientboundSoundPacket pkt = new ClientboundSoundPacket(holder, SoundSource.MASTER, pos.getX(), pos.getY(), pos.getZ(), 1f, 1f, player.level().getRandom().nextLong());
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.connection.send(pkt);
        }
    }


}
