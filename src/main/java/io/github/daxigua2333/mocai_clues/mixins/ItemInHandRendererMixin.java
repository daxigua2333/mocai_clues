package io.github.daxigua2333.mocai_clues.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.networks.FinderLeaveHandPayload;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    Map<UUID, Item> prevMainItemMap = new HashMap<>();
    Map<UUID, Item> prevOffItemMap = new HashMap<>();

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void renderArmWithItem(
        AbstractClientPlayer player,
        float partialTicks,
        float pitch,
        InteractionHand hand,
        float swingProgress,
        ItemStack stack,
        float equippedProgress,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int combinedLight,
        CallbackInfo ci
    ) {
        if (hand == InteractionHand.MAIN_HAND) {
            Item prevItem = prevMainItemMap.getOrDefault(player.getUUID(), Items.AIR.asItem());
            var type = ModItemsRegistry.CLUE_FINDER_ITEM.get();
            if (prevItem != stack.getItem()) {
                if (prevItem == type){
                    PacketDistributor.sendToServer(new FinderLeaveHandPayload(true));
                }
            }
            prevMainItemMap.put(player.getUUID(), stack.getItem());
        }
        if (hand == InteractionHand.OFF_HAND) {
            Item prevItem = prevOffItemMap.getOrDefault(player.getUUID(), Items.AIR.asItem());
            var type = ModItemsRegistry.CLUE_FINDER_ITEM.get();
            if (prevItem != stack.getItem()) {
                if (prevItem == type){
                    PacketDistributor.sendToServer(new FinderLeaveHandPayload(false));
                }
            }
            prevOffItemMap.put(player.getUUID(), stack.getItem());
        }
    }
}
