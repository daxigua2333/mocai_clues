//package io.github.daxigua2333.mocai_clues.entry.client;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import net.minecraft.client.Camera;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.multiplayer.ClientLevel;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.culling.Frustum;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.phys.AABB;
//import net.minecraft.world.phys.Vec3;
//import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
//import org.joml.Matrix3f;
//import org.joml.Matrix4f;
//
//public final class BlockOutlineRenderer {
//
//    private static final double MAX_DIST_SQ = 96.0 * 96.0;
//    private static final float R = 0.1f, G = 0.8f, B = 1.0f, A = 1.0f;
//    private static final double EPS = 0.002; // reduce z-fighting
//
//    public static void render(RenderLevelStageEvent event) {
//        final PoseStack poseStack = event.getPoseStack();
//        if (poseStack == null) return;
//
//        final Minecraft mc = Minecraft.getInstance();
//        final ClientLevel level = mc.level;
//        if (level == null) return;
//
//        final Camera camera = event.getCamera();
//        final Vec3 camPos = camera.getPosition();
//
//        // Prepare transforms: world -> view
//        poseStack.pushPose();
//        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
//
//        // Batch into the lines buffer
//        final MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
//        final VertexConsumer vc = buffers.getBuffer(RenderType.lines());
//
//        final Matrix4f pose = poseStack.last().pose();
//        final Matrix3f normalMat = poseStack.last().normal();
//
//        // Optional: thicker lines (may be clamped by drivers)
//        // RenderSystem.lineWidth(2.0f);
//
//        final Frustum frustum = event.getFrustum();
//
//        for (var it = HIGHLIGHT.iterator(); it.hasNext(); ) {
//            final long packed = it.nextLong();
//            final int x = BlockPos.getX(packed);
//            final int y = BlockPos.getY(packed);
//            final int z = BlockPos.getZ(packed);
//
//            // Distance cull (center-of-block)
//            final double cx = x + 0.5 - camPos.x;
//            final double cy = y + 0.5 - camPos.y;
//            final double cz = z + 0.5 - camPos.z;
//            final double distSq = cx*cx + cy*cy + cz*cz;
//            if (distSq > MAX_DIST_SQ) continue;
//
//            // Frustum cull in world space
//            AABB aabb = new AABB(x, y, z, x + 1, y + 1, z + 1).inflate(EPS);
//            if (!frustum.isVisible(aabb)) continue;
//
//            emitAabbLines(vc, pose, normalMat, aabb, R, G, B, A);
//        }
//
//        // Flush only what we used
//        buffers.endBatch(RenderType.lines());
//
//        poseStack.popPose();
//
//    }
//
//
//
//    private static void emitAabbLines(
//            VertexConsumer vc, Matrix4f pose, Matrix3f normalMat,
//            AABB b, float r, float g, float bl, float a
//    ) {
//        final float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
//        final float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;
//
//        // 12 edges
//        line(vc, pose, normalMat, x1,y1,z1, x2,y1,z1, r,g,bl,a);
//        line(vc, pose, normalMat, x2,y1,z1, x2,y1,z2, r,g,bl,a);
//        line(vc, pose, normalMat, x2,y1,z2, x1,y1,z2, r,g,bl,a);
//        line(vc, pose, normalMat, x1,y1,z2, x1,y1,z1, r,g,bl,a);
//
//        line(vc, pose, normalMat, x1,y2,z1, x2,y2,z1, r,g,bl,a);
//        line(vc, pose, normalMat, x2,y2,z1, x2,y2,z2, r,g,bl,a);
//        line(vc, pose, normalMat, x2,y2,z2, x1,y2,z2, r,g,bl,a);
//        line(vc, pose, normalMat, x1,y2,z2, x1,y2,z1, r,g,bl,a);
//
//        line(vc, pose, normalMat, x1,y1,z1, x1,y2,z1, r,g,bl,a);
//        line(vc, pose, normalMat, x2,y1,z1, x2,y2,z1, r,g,bl,a);
//        line(vc, pose, normalMat, x2,y1,z2, x2,y2,z2, r,g,bl,a);
//        line(vc, pose, normalMat, x1,y1,z2, x1,y2,z2, r,g,bl,a);
//    }
//
//    private static void line(
//            VertexConsumer vc, Matrix4f pose, Matrix3f normalMat,
//            float x1, float y1, float z1,
//            float x2, float y2, float z2,
//            float r, float g, float b, float a
//    ) {
//        // For the lines shader, the "normal" is treated as direction (start -> end) for thick lines.
//        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
//
//        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).normal(normalMat, dx, dy, dz).endVertex();
//        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).normal(normalMat, dx, dy, dz).endVertex();
//    }
//
//    // Call these from your own logic
//    public static void setHighlighted(LongOpenHashSet newSet) {
//        HIGHLIGHT.clear();
//        HIGHLIGHT.addAll(newSet);
//    }
//}
