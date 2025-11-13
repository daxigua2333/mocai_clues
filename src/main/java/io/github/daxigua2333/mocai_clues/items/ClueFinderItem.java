package io.github.daxigua2333.mocai_clues.items;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.guis.ClueInventoryMenu;
import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainer;
import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainerAttachmentHelper;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.statics.ClueContainerSearchUtils;
import io.github.daxigua2333.mocai_clues.networks.FinderHitResultPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ClueFinderItem extends Item {
    public ClueFinderItem(Properties props) {
        super(props);
    }

    // texture reflection
    public static void registerTextureChange(){
        ItemProperties.register(ModItemsRegistry.CLUE_FINDER_ITEM.get(),
        ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "found"),
        (ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int id) -> {
//        (stack, level, entity, id) -> {
            if (entity != null) {
                return stack.getOrDefault(ModDataComponentsRegistry.CURRENT_FINDER_HIT_RESULT.get(), false) ? 1.0F : 0.0F;
            } else {
                // entity == null (e.g., in item frame/JEI/creative preview) – fall back to stack data
                return 0.0F;
            }
        });
    };
    // sound reflection
    public static void handleHitResult(FinderHitResultPayload data, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            ItemStack stack = main.getItem() == ModItemsRegistry.CLUE_FINDER_ITEM.get() ? main : off;
            if (stack == ItemStack.EMPTY) return;
            var prev = ModDataComponentsRegistry.PREV_FINDER_HIT_RESULT.get();
            var current = ModDataComponentsRegistry.CURRENT_FINDER_HIT_RESULT.get();
            stack.set(prev, stack.getOrDefault(current, false));
            stack.set(current, data.success());

            if (!stack.getOrDefault(prev,false) && stack.getOrDefault(current, false)) {
                player.level().playSound(player, player.getOnPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1f, 1f);
            }
        });
    };


    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide) {
            // client simply return success (server will actually open)
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // server side
        // Make sure container exists
        if (!ClueContainerAttachmentHelper.containsKey(level, clickedPos)) {
            player.displayClientMessage(Component.literal("No inventory attachment at this block."), true);
            return InteractionResult.SUCCESS;
        }

        // Obtain or create the attachment instance
        ClueContainer container = ClueContainerAttachmentHelper.getOrCreate(level, clickedPos);
        LevelChunk chunk = level.getChunkAt(clickedPos);
        // Create a MenuProvider which will be used server-side to create the container
        MenuProvider provider = new SimpleMenuProvider(
            (id, playerInv, p) -> new ClueInventoryMenu(id, playerInv, container.getInv(chunk)),
            Component.literal(container.getName().isEmpty() ? "Clue Inventory" : container.getName())
        );
        // Open screen for server player and write initial sync data to the buffer for the client constructor:
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.openMenu(provider);

        return InteractionResult.SUCCESS;
    }
}
