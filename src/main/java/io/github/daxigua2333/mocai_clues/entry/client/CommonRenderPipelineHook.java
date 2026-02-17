//package io.github.daxigua2333.mocai_clues.entry.client;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.math.Axis;
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.component.ComponentType;
//import io.github.daxigua2333.mocai_clues.component.data.ItemClue;
//import io.github.daxigua2333.mocai_clues.data.ObjectsWithLocation;
//import io.github.daxigua2333.mocai_clues.data.common.DataManager;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.client.renderer.LevelRenderer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.texture.OverlayTexture;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.item.ItemDisplayContext;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.phys.Vec3;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.api.distmarker.OnlyIn;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
//
//import java.util.List;
//
//@OnlyIn(Dist.CLIENT)
//@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
//public final class CommonRenderPipelineHook {
//    private static final int CHUNK_RADIUS = 5;
//
//    @SubscribeEvent
//    public static void onRenderLevelStage(RenderLevelStageEvent event) {
//        ItemWorldRenderer(event);
//    }
//
//    private static void ItemWorldRenderer(RenderLevelStageEvent event) {
//        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
//        Minecraft mc = Minecraft.getInstance();
//        ClientLevel level = mc.level;
//        LocalPlayer player = mc.player;
//        if (level == null || player == null) return;
//        PoseStack poseStack = event.getPoseStack();
//        Vec3 camPos = event.getCamera().getPosition();
//        // Use our own immediate buffer so we can flush right here (no interference with vanilla’s buffer lifecycle)
////        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
//        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
//        // Iterate your already-prepared data here:
//        // TODO: Ill just assume that only manual clue can be item type
//        List<ObjectsWithLocation> data = DataManager.Client.retrieveByNearbyLoadedChunks(level, player.chunkPosition(), Math.min(CHUNK_RADIUS, Minecraft.getInstance().options.renderDistance().get()));
//        for (ObjectsWithLocation ol : data) {
//            for (ClueObject obj : ol.objects()) {
//                ItemClue compo = obj.getComponent(ComponentType.ITEM_CLUE);
//                if (compo == null) continue;
//
//                BlockPos pos = compo.getPos();
//                Direction face = compo.getFace();
//                if (pos == null || face == null) continue;
//                // TODO: culling
//                if (!event.getFrustum().isVisible(new AABB(pos))) continue;
//
//                poseStack.pushPose();
//                poseStack.translate(
//                        pos.getX() - camPos.x,
//                        pos.getY() - camPos.y,
//                        pos.getZ() - camPos.z
//                );
//                applyFaceTransform(poseStack, pos, face);
//
//                int packedLight = LevelRenderer.getLightColor(level, pos.relative(face));
//
////            for (int i = 0; i < 4; i++) {
//////            for (ItemStack stack : compo.getStacks()) {
////                ItemStack stack = compo.getStacks().get(i);
////                if (stack.isEmpty()) continue;
////                mc.getItemRenderer().renderStatic(
////                        stack,
////                        ItemDisplayContext.FIXED,
////                        packedLight,
////                        OverlayTexture.NO_OVERLAY,
////                        poseStack,
////                        bufferSource,
////                        level,
////                        0
////                );
////            }
//                mc.getItemRenderer().renderStatic(
//                        compo.getStack(),
//                        ItemDisplayContext.FIXED,
//                        packedLight,
//                        OverlayTexture.NO_OVERLAY,
//                        poseStack,
//                        bufferSource,
//                        level,
//                        0
//                );
//
//
//                poseStack.popPose();
//            }
//        }
//    }
//
//    // Helper to get a deterministic random based on position
//    // We add the face index to the seed so different faces on the same block look different
//    private static RandomSource getSeededRandom(BlockPos pos, Direction face) {
//        long seed = pos.asLong() + face.get3DDataValue();
//        return RandomSource.create(seed);
//    }
//
//    private static void applyFaceTransform(PoseStack poseStack, BlockPos pos, Direction face) {
//        RandomSource random = getSeededRandom(pos, face);
//        // 1. Base translation to the center of the block
//        poseStack.translate(0.5, 0.5, 0.5);
//        // 2. Orient the PoseStack coordinate system to the Face
//        // After this block, 'Z' is pointing "out" from the face, 'X' and 'Y' are the face surface
//        applyFaceOrientation(poseStack, face);
//        // 3. Apply the "Surface" Offset (Depth)
//        // Move slightly out from the block to prevent Z-fighting
//        poseStack.translate(0, 0, 0.51f);
//        // 4. APPLY RANDOMNESS
//        // Randomly shift the item on the X and Y axis of the face (limit to +/- 0.2 to stay on block)
//        float randX = (random.nextFloat() - 0.5f) * 0.4f;
//        float randY = (random.nextFloat() - 0.5f) * 0.4f;
//        poseStack.translate(randX, randY, 0);
//        // Random rotation around the Z-axis (the normal of the face)
//        // Adjust 360f to something smaller (e.g., 20f) if you want them mostly upright
//        float randRot = (random.nextFloat() - 0.5f) * 360f;
//        poseStack.mulPose(Axis.ZP.rotationDegrees(randRot));
//        // 5. Final Scaling
//        poseStack.scale(0.8f, 0.8f, 0.8f);
//    }
//
//    private static void applyFaceOrientation(PoseStack poseStack, Direction face) {
//        switch (face) {
//            case DOWN -> {
//                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
//                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
//            }
//            case UP -> {
//                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
//                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
//            }
//            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
//            case SOUTH -> { /* Default */ }
//            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
//            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
//        }
//    }
//    // Call this inside your onRenderLevelStage loop
//    // applyFaceTransform(poseStack, pos, itemData.face);
//}