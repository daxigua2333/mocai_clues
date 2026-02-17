package io.github.daxigua2333.mocai_clues.component.system.discovery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentFamilyRegistry;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.ItemClue;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractPassiveBehavior;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosWithFace;
import io.github.daxigua2333.mocai_clues.component.world.finder.FinderState;
import io.github.daxigua2333.mocai_clues.data.ObjectsWithLocation;
import io.github.daxigua2333.mocai_clues.data.common.DataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public final class InteractPassiveSystem {
    private static final int CHUNK_RADIUS = 5;
    public static final ComponentFamilyRegistry.SystemFamily FAMILY = ComponentFamilyRegistry.SystemFamily.INTERACT_PASSIVE_SYSTEM;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || mc.isPaused()) return;
        // don’t spawn every tick; every 4 ticks is plenty for a “flash”
        if ((level.getGameTime() & 3) != 0) return;

        List<ObjectsWithLocation> data = DataManager.Client.retrieveByNearbyLoadedChunks(level, mc.player.chunkPosition(), Math.min(CHUNK_RADIUS, Minecraft.getInstance().options.renderDistance().get()));
        for (ObjectsWithLocation ol : data) {
            processPassiveBehavior(ol, InteractPassiveBehavior.BehaviorType.FLASH_DOT, mc.player,
                    obj -> spawnFlashDot(level, obj));
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) return;
        // Use our own immediate buffer so we can flush right here (no interference with vanilla’s buffer lifecycle)
//        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        List<ObjectsWithLocation> data = DataManager.Client.retrieveByNearbyLoadedChunks(level, player.chunkPosition(), Math.min(CHUNK_RADIUS, Minecraft.getInstance().options.renderDistance().get()));
        for (ObjectsWithLocation ol : data) {
            processPassiveBehavior(ol, InteractPassiveBehavior.BehaviorType.ITEM_RENDERER, player,
                    obj -> renderItem(obj, event, bufferSource));
        }
    }


    private static void processPassiveBehavior(ObjectsWithLocation ol, InteractPassiveBehavior.BehaviorType type, LocalPlayer player, Consumer<ClueObject> executeBehavior) {
        for (ClueObject obj : ol.objects()) {
            if (!obj.hasFamily(FAMILY)) {
                continue;
            }

            // FinderState accessibility check
            FinderState bCompo = obj.getComponentOrThrow(ComponentType.FINDER_STATE);
            if (!bCompo.isAccessible(player.getScoreboardName())) {
                continue;
            }

            InteractPassiveBehavior pCompo = obj.getComponentOrThrow(ComponentType.INTERACT_PASSIVE_BEHAVIOR);
            if (!pCompo.hasBehavior(type)) {
                continue;
            }

            executeBehavior.accept(obj);
//            switch (type) {
//                case FLASH_DOT -> spawnFlashDot(level, obj);
//                case ITEM_RENDERER -> renderItem();
//            }
        }
    }


    private static void spawnFlashDot(ClientLevel level, ClueObject obj) {
        FinderState sCompo = obj.getComponentOrThrow(ComponentType.FINDER_STATE);
        if (!sCompo.isDoRenderFlashDot()) {
            return;
        }
        if (!sCompo.isAccessible(Minecraft.getInstance().player.getScoreboardName())) {
            return;
        }

        BlockPosWithFace pCompo = obj.getComponentOrThrow(ComponentType.BLOCK_POS_WITH_FACE);
        BlockPos pos = pCompo.getPos();
        Direction face = pCompo.getFace();
        if (pos == null || face == null) return;
        final double EPS = 0.55; // slightly outside the block surface
        double x = pos.getX() + 0.5 + face.getStepX() * EPS;
        double y = pos.getY() + 0.5 + face.getStepY() * EPS;
        double z = pos.getZ() + 0.5 + face.getStepZ() * EPS;
        float r = Config.CLIENT.FLASH_DOT_R.getAsInt() / 255f;
        float g = Config.CLIENT.FLASH_DOT_G.getAsInt() / 255f;
        float b = Config.CLIENT.FLASH_DOT_B.getAsInt() / 255f;
//        var dust = new DustParticleOptions(new Vector3f(1.0f, 0.9f, 0.2f), 1f);
        var dust = new DustParticleOptions(new Vector3f(r, g, b), 1f);
        // "true" = alwaysRender (ignores “Minimal” particles setting)
        level.addParticle(dust, true, x, y, z, 0.0, 0.0, 0.0);


    }


    private static void renderItem(ClueObject obj, RenderLevelStageEvent event, MultiBufferSource.BufferSource bufferSource) {
        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();
        ItemClue iCompo = obj.getComponentOrThrow(ComponentType.ITEM_CLUE);
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        BlockPosWithFace pCompo = obj.getComponentOrThrow(ComponentType.BLOCK_POS_WITH_FACE);
        BlockPos pos = pCompo.getPos();
        Direction face = pCompo.getFace();
        if (pos == null || face == null) return;

        // TODO: culling
        if (!event.getFrustum().isVisible(new AABB(pos))) return;

        poseStack.pushPose();
        poseStack.translate(
                pos.getX() - camPos.x,
                pos.getY() - camPos.y,
                pos.getZ() - camPos.z
        );
        applyFaceTransform(poseStack, pos, face);

        int packedLight = LevelRenderer.getLightColor(level, pos.relative(face));

        mc.getItemRenderer().renderStatic(
                iCompo.getStack(),
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                level,
                0
        );

        poseStack.popPose();
    }

    private static void applyFaceTransform(PoseStack poseStack, BlockPos pos, Direction face) {
        RandomSource random = getSeededRandom(pos, face);
        // 1. Base translation to the center of the block
        poseStack.translate(0.5, 0.5, 0.5);
        // 2. Orient the PoseStack coordinate system to the Face
        // After this block, 'Z' is pointing "out" from the face, 'X' and 'Y' are the face surface
        applyFaceOrientation(poseStack, face);
        // 3. Apply the "Surface" Offset (Depth)
        // Move slightly out from the block to prevent Z-fighting
        poseStack.translate(0, 0, 0.51f);
        // 4. APPLY RANDOMNESS
        // Randomly shift the item on the X and Y axis of the face (limit to +/- 0.2 to stay on block)
        float randX = (random.nextFloat() - 0.5f) * 0.4f;
        float randY = (random.nextFloat() - 0.5f) * 0.4f;
        poseStack.translate(randX, randY, 0);
        // Random rotation around the Z-axis (the normal of the face)
        // Adjust 360f to something smaller (e.g., 20f) if you want them mostly upright
        float randRot = (random.nextFloat() - 0.5f) * 360f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(randRot));
        // 5. Final Scaling
        poseStack.scale(0.8f, 0.8f, 0.8f);
    }

    private static void applyFaceOrientation(PoseStack poseStack, Direction face) {
        switch (face) {
            case DOWN -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            case UP -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case SOUTH -> { /* Default */ }
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        }
    }

    // Helper to get a deterministic random based on position
    // We add the face index to the seed so different faces on the same block look different
    private static RandomSource getSeededRandom(BlockPos pos, Direction face) {
        long seed = pos.asLong() + face.get3DDataValue();
        return RandomSource.create(seed);
    }

}
