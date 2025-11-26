package io.github.daxigua2333.mocai_clues.footprints.statics;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.footprints.Footprint;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class CreateEventHooks {

//    @SubscribeEvent
//    public static void onLivingTicking(EntityTickEvent.Post event) {
//        Entity entity = event.getEntity();
//        if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
//            handleMovingEntity(living);
//        }
//    }

    @SubscribeEvent
    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        handleMovingEntity(player);
    }

    private static void handleMovingEntity(LivingEntity entity) {
        // TODO: config
        if (entity.tickCount % 20 != 0) {
            return;
        }

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
        Vec3 feet = box.getBottomCenter().add(0.0D, 0.01D, 0.0D);

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
        FootprintServerHelper.create(level, feet.x, feet.y , feet.z, yaw, longSide, shortSide, 1);

    }

    private static boolean entityIsMoving(Entity e) {
        double dx = e.getX() - e.xo;
        double dy = e.getY() - e.yo;
        double dz = e.getZ() - e.zo;
        return dx * dx + dy * dy + dz * dz > 1.0E-6;
    }

}
