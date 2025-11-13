package io.github.daxigua2333.mocai_clues.items.statics;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainerAttachmentHelper;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.networks.FinderHitResultPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class FinderHitResultTicker {

    // ticker
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (main.getItem() != ModItemsRegistry.CLUE_FINDER_ITEM.get() && off.getItem() != ModItemsRegistry.CLUE_FINDER_ITEM.get()) return;

        // raytrace: player.pick(range, partialTicks, ClipContext.Fluid.NONE)
        HitResult hr = player.pick(Config.FINDER_HIT_DISTANCE.get(), 0.0F, false);  // TODO: config
        if (hr.getType() != HitResult.Type.BLOCK) return;

        // optional: check which face was hit: bhr.getDirection()

        BlockHitResult bhr = (BlockHitResult) hr;
        BlockPos pos = bhr.getBlockPos();
        // notify client
        if (ClueContainerAttachmentHelper.containsKey(player.level(), pos)) {
            PacketDistributor.sendToPlayer((ServerPlayer)player, new FinderHitResultPayload(true));
        }else{
            PacketDistributor.sendToPlayer((ServerPlayer)player, new FinderHitResultPayload(false));
        }
    }


}
