package io.github.daxigua2333.mocai_clues.items;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.guis.ClueInventoryInfiniteMenu;
import io.github.daxigua2333.mocai_clues.guis.ClueInventoryMenu;
import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainer;
import io.github.daxigua2333.mocai_clues.data_attachments.statics.ClueContainerAttachmentHelper;
import io.github.daxigua2333.mocai_clues.items.components.FinderHitResult;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.statics.FinderHitResultTicker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

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
                return stack.getOrDefault(ModDataComponentsRegistry.FINDER_HIT_RESULT.get(), new FinderHitResult(false, false)).current() ? 1.0F : 0.0F;
            } else {
                // entity == null (e.g., in item frame/JEI/creative preview) – fall back to stack data
                return 0.0F;
            }
        });
    };

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide()) {
            Player player = (Player) entity;
            if (stack.getItem() == ModItemsRegistry.CLUE_FINDER_ITEM.get() && !selected && stack != player.getOffhandItem()) {
                FinderHitResultTicker.handleHitResult(player, stack, false);
            }
        }
    }

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
            player.displayClientMessage(Component.literal("No inventory attachment at this block."), true);  // TODO: lang
            return InteractionResult.SUCCESS;
        }

        // Obtain or create the attachment instance
        ClueContainer container = ClueContainerAttachmentHelper.getOrCreate(level, clickedPos);
        LevelChunk chunk = level.getChunkAt(clickedPos);
        // Create a MenuProvider which will be used server-side to create the container
        MenuProvider provider;
        if (container.isInfinite()) {
            provider = new SimpleMenuProvider(
                (id, playerInv, p) -> new ClueInventoryInfiniteMenu(id, playerInv, container.getInv(chunk), clickedPos, player.isCreative()),
                Component.literal(container.getName().isEmpty() ? "Clue Inventory (Infinite)" : container.getName())  // TODO: lang
            );
        } else {
            provider = new SimpleMenuProvider(
                (id, playerInv, p) -> new ClueInventoryMenu(id, playerInv, container.getInv(chunk), clickedPos, player.isCreative()),
                Component.literal(container.getName().isEmpty() ? "Clue Inventory" : container.getName())
            );
        }

        // Open screen for server player and write initial sync data to the buffer for the client constructor:
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.openMenu(provider);

        return InteractionResult.SUCCESS;
    }
}
