package io.github.daxigua2333.cmagic_clue.items;

import io.github.daxigua2333.cmagic_clue.guis.ClueInventoryInfiniteMenu;
import io.github.daxigua2333.cmagic_clue.guis.ClueInventoryMenu;
import io.github.daxigua2333.cmagic_clue.data_attachments.ClueContainer;
import io.github.daxigua2333.cmagic_clue.data_attachments.statics.ClueContainerAttachmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class ClueFinderItem extends Item {
    public ClueFinderItem(Properties props) {
        super(props);
    }


    @Override
    public InteractionResult useOn(UseOnContext context) {
//        RectOverlayRenderer.ACTIVE_RECTS = Arrays.asList(new RectOverlay(0f, -60f, 0f, 30, 2, 1, 0.8f));
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
