package io.github.daxigua2333.cmagic_clue.footprints.statics;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.Config;
import io.github.daxigua2333.cmagic_clue.footprints.data.TimestampSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CMagicClue.MODID)
public final class CreateEventHooks {
    private static final double d = 0.01D;

//    @SubscribeEvent
//    public static void onLivingTicking(EntityTickEvent.Post event) {
//        Entity entity = event.getEntity();
//        if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
//            handleMovingEntity(living);
//        }
//    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!HookToggle.isEnabled()) {return;}

        int partialTick = Config.SERVER.FOOTPRINT_CREATE_FREQUENCY.getAsInt();
        if (player.tickCount % partialTick != 0) {
            return;
        }

//        player.level().getProfiler().push("mocai_clues:create_events");
        handleMovingEntity(player);
//        player.level().getProfiler().pop();
    }

    @SubscribeEvent
    public static void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        if (!HookToggle.isEnabled()) {return;}
        LivingEntity living = event.getEntity();
        if (!(living instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        handleMovingEntity(player);
    }

    private static void handleMovingEntity(LivingEntity entity) {
        Level level = entity.level();
        if (level.isClientSide) {
            return;
        }

        if (!entity.isAlive()) return;
        if (!entity.onGround()) return;
        if (!entity.verticalCollisionBelow) return;  // make sure collide with ground
        if (entityIsMoving(entity)) {  // Only when actually moving horizontally
            return;
        }

        // === Feet position (bottom center of hitbox) ===
        AABB box = entity.getBoundingBox();
        // Bottom center of the bounding box, nudged slightly up to avoid z-fighting
        BlockPos blockBelow = entity.getOnPos();
        Vec3 rawFeet = box.getBottomCenter();
        Vec3 feet = new Vec3(adjustXZOffset(blockBelow.getX(), rawFeet.x), rawFeet.y + d, adjustXZOffset(blockBelow.getZ(), rawFeet.z));

        // === Horizontal rotation ===
        // Simple: use the entity's yaw. (You could also derive from motion if you prefer.)
        float yaw = entity.getYRot();

        // === Size-based footprint long/short side ===
        double xSize = box.getXsize();
        double zSize = box.getZsize();
        double max = Math.max(xSize, zSize);
        double min = Math.min(xSize, zSize);
        // Scale however you like; these are just sane defaults.
        float longSide  = (float) (max * 0.6D); // along facing/move direction
        float shortSide = (float) (min * 0.6D); // across the foot

        // === Spawn your footprint ===
//        Footprint footprint = new Footprint(feet.x, feet.y, feet.z, yaw, longSide, shortSide, 1);
        FootprintServerHelper.create(level, blockBelow, feet.x, feet.y , feet.z, yaw,
                longSide * 1.25f, shortSide, (float) Config.SERVER.FOOTPRINT_INIT_ALPHA.getAsDouble(),
                TimestampSavedData.getInstance((ServerLevel) level).getTimestamp(),
                FootprintServerHelper.createLifetime(level, blockBelow),
                entity.getUUID()
        );

    }

    private static boolean entityIsMoving(Entity e) {
        double dx = e.getX() - e.xo;
        double dy = e.getY() - e.yo;
        double dz = e.getZ() - e.zo;
        return dx * dx + dy * dy + dz * dz > 1.0E-6;
    }

    private static double adjustXZOffset(int blockPos, double coordinate) {
        if (Math.floor(coordinate) < blockPos) {
            return blockPos;
        }
        if (Math.floor(coordinate) > blockPos) {
            return blockPos + 1 - d;
        }
        return coordinate;
    }

}
