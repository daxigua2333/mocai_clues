package io.github.daxigua2333.mocai_clues.items;

import net.minecraft.world.item.Item;


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


}
