package io.github.daxigua2333.mocai_clues.items;

import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainer;
import io.github.daxigua2333.mocai_clues.data_attachments.statics.ClueContainerAttachmentHelper;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;


public class ClueWandItem extends Item {
    public ClueWandItem(Properties props) {
        super(props);
    }

//    // LEFT-CLICK: cycle modes when player swings (left click)
//    @Override
//    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
//        if (!(entity instanceof Player player)) return false;
//        Level level = player.level();
//        // Only modify state on server side (avoid double-cycling)
//        if (!level.isClientSide) {
////            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1f, 1f);
//
//            // update(...) will set the component (creates it if needed) and returns the old/updated value
//            stack.update(ModDataComponents.WAND_MODE.get(), WandMode.CREATE, current -> {
//                // If the component was missing current could be null - guard
//                WandMode cur = current == null ? WandMode.CREATE : current;
//                return cur.next();
//            });
//
//            WandMode newMode = stack.getOrDefault(ModDataComponents.WAND_MODE.get(), WandMode.CREATE);
//
//            // show simple chat feedback (server -> client message to the player)
//            player.displayClientMessage(Component.literal("Wand mode: " + newMode.toString()), true);
//        }
//        // return true to cancel further processing if you don't want the swing to hit; adjust as needed
//        return false;
//    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        WandMode mode = stack.getOrDefault(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE);
        switch (mode) {
            case CREATE:
                if (!level.isClientSide()) {
                    if (ClueContainerAttachmentHelper.containsKey(level, clickedPos)) {
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(clickedPos.toShortString() + " already has data"));  // TODO: lang
                        }
                    } else {
                        ClueContainerAttachmentHelper.getOrCreate(level, clickedPos);
                        if (player != null) {
                            player.sendSystemMessage(Component.literal("Attached data at " + clickedPos.toShortString()));
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            case DELETE:
                if (!level.isClientSide()) {
                    ClueContainerAttachmentHelper.remove(level, clickedPos);
                    if (player != null) {
                        player.sendSystemMessage(Component.literal("Removed data at " + clickedPos.toShortString()));  // TODO: lang
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
//            case QUERY:
//                if (!level.isClientSide()) {
//                    // TODO: the nomi gui
//                }
            case CREATE_INFINITY:
                if (!level.isClientSide()) {
                    if (ClueContainerAttachmentHelper.containsKey(level, clickedPos)) {
                        if (player != null) {
                            player.sendSystemMessage(Component.literal(clickedPos.toShortString() + " already has data"));  // TODO: lang
                        }
                    } else {
                        ClueContainer container = ClueContainerAttachmentHelper.getOrCreate(level, clickedPos);
                        container.setInfinite(true);
                        if (player != null) {
                            player.sendSystemMessage(Component.literal("Attached data at " + clickedPos.toShortString()));
                        }
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            case null, default:
                return InteractionResult.PASS;
        }
    }

}
