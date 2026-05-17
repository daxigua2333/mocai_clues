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

            BlockHitResult bhr = getPlayerLookHitPosition(serverPlayer);

            if (bhr != null) {
                byte[] redColor = new byte[]{(byte) 255, 0, 0, (byte) 255}; // TODO: palette

                int bytesPerPixel = DecalLayerHolder.BYTE_PER_PIXEL;
                int startIndex = calModifiedPixelIndexAtVec3(bhr.getBlockPos(), bhr.getDirection(), bhr.getLocation(), bytesPerPixel);

                DecalLayerHolder holder = level.getChunkAt(bhr.getBlockPos()).getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER);
                holder.pushOnePixelLayerAt((ServerLevel) level, bhr.getBlockPos(), bhr.getDirection(), redColor, startIndex);
            }
        }
    }


    /**
     * Gets the exact Vec3 coordinate where the player is looking at a block.
     *
     * @param player The ServerPlayer to check.
     * @return The exact Vec3 coordinate of the hit, or null if looking at air.
     */
    @Nullable
    public static BlockHitResult getPlayerLookHitPosition(ServerPlayer player) {
        Level level = player.level();

        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle();
        double reachDistance = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        Vec3 endPosition = eyePosition.add(
                lookVector.x * reachDistance,
                lookVector.y * reachDistance,
                lookVector.z * reachDistance
        );

        ClipContext context = new ClipContext(
                eyePosition,
                endPosition,
                ClipContext.Block.OUTLINE, // Collides with block hitboxes
                ClipContext.Fluid.NONE,    // Change to ANY or SOURCE_ONLY if you want to hit water
                player                     // The entity doing the tracing
        );

        BlockHitResult hitResult = level.clip(context);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return hitResult;
        }

        return null;
    }


    /**
     * Modifies a specific pixel in a 16x16 texture byte array based on a 3D world coordinate.
     *
     * @param pos           The position of the block.
     * @param face          The face of the block the hit occurred on.
     * @param hitVec        The exact 3D point on the face quad.
     * @param bytesPerPixel How many bytes make up one pixel (usually 4 for RGBA).
     */
    private static int calModifiedPixelIndexAtVec3(BlockPos pos, Direction face, Vec3 hitVec, int bytesPerPixel) {

        // 1. Get the local coordinate of the hit relative to the block's corner (0.0 to 1.0)
        double dx = hitVec.x - pos.getX();
        double dy = hitVec.y - pos.getY();
        double dz = hitVec.z - pos.getZ();
        double u = 0.0;
        double v = 0.0;
        // 2. Map the 3D local coordinates to 2D UV coordinates (0.0 to 1.0)
        // Note: UV origin (0,0) is usually the top-left of the texture.
        switch (face) {
            case NORTH: // -Z face
                u = 1.0 - dx;
                v = 1.0 - dy;
                break;
            case SOUTH: // +Z face
                u = dx;
                v = 1.0 - dy;
                break;
            case WEST: // -X face
                u = dz;
                v = 1.0 - dy;
                break;
            case EAST: // +X face
                u = 1.0 - dz;
                v = 1.0 - dy;
                break;
            case UP: // +Y face
                u = dx;
                v = dz;
                break;
            case DOWN: // -Y face
                u = dx;
                v = 1.0 - dz;
                break;
        }
        // 3. Convert UV (0.0 - 1.0) to Pixel Coordinates (0 - 15)
        // We use Mth.clamp to guarantee we don't get an out-of-bounds index (like 16)
        // if the hitVec is perfectly on the upper boundary (1.0).
        int pixelX = Mth.clamp((int) (u * 16.0), 0, 15);
        int pixelY = Mth.clamp((int) (v * 16.0), 0, 15);
        // 4. Calculate the 1D array index
        // Formula: (y * width + x) * bytes_per_pixel
        int startIndex = (pixelY * 16 + pixelX) * bytesPerPixel;
        return startIndex;
        //        // 5. Replace the pixel data
//        // Safety check to prevent array out-of-bounds crashes
//        if (startIndex + bytesPerPixel <= textureData.length && newPixelData.length >= bytesPerPixel) {
//            for (int i = 0; i < bytesPerPixel; i++) {
//                textureData[startIndex + i] = newPixelData[i];
//            }
//        }
    }
}
