package io.github.daxigua2333.cmagic_clue.decal;

import io.github.daxigua2333.cmagic_clue.data.ModAttachmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class PainterItem extends Item {

    public PainterItem(Properties properties) {
        super(properties);
    }

    /**
     * Called when the player first right-clicks.
     * We use this to tell the game to start the "Using" state.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // This is crucial. It tells the game to start counting ticks for onUseTick
        player.startUsingItem(hand);

        return InteractionResultHolder.consume(itemStack);
    }

    /**
     * Determines how long the item can be used continuously.
     * 72000 is the standard for continuous items (like bows/shields). It equals 1 hour of holding.
     */
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    /**
     * Optional: Determines what animation the player makes while holding right click.
     * NONE, BOW, SPEAR, BLOCK (shield), etc.
     */
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE; // Change to BOW if you want them to pull it back like a bow
    }

    /**
     * This fires exactly once per tick (20 times a second) while the player holds right-click.
     */
    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide() && livingEntity instanceof ServerPlayer serverPlayer) {
            ServerLogicHooks.onPainterUse((ServerLevel) level, serverPlayer);
        }
    }
}
