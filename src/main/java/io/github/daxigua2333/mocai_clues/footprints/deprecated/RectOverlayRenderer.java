package io.github.daxigua2333.mocai_clues.footprints.deprecated;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.List;

@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public final class RectOverlayRenderer {

    // You manage this list from elsewhere (thread-safe if you modify it from game thread only).
    public static List<RectOverlay> ACTIVE_RECTS = List.of();

    // Max distance to draw (simple distance-based culling)
    private static final float MAX_DIST_SQ = 128.0f * 128.0f;


    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
//        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {}
        // If you don't want to render in some dimensions, check here.
        LevelRenderer levelRenderer = event.getLevelRenderer();
        if (ACTIVE_RECTS.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        if (poseStack == null) return;

        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();

        // OpenGL state for translucent colored quads
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Use POSITION_COLOR format, single draw call
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        poseStack.pushPose();
        // move world so that camera is origin
        poseStack.translate(-cam.x, -cam.y, -cam.z);

        renderBatch(ACTIVE_RECTS, poseStack, buffer, cam);

        poseStack.popPose();

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        // Leave blending enabled if you rely on vanilla state afterwards,
        // or disable here if you know you don't:
        // RenderSystem.disableBlend();
    }

    private static void renderBatch(List<RectOverlay> rects,
                                    PoseStack poseStack,
                                    VertexConsumer vc,
                                    Vec3 camPos) {
        PoseStack.Pose pose = poseStack.last();

        for (RectOverlay r : rects) {
            // Simple distance culling; uses world coords
            float dx = r.x - (float) camPos.x;
            float dz = r.z - (float) camPos.z;
            if (dx * dx + dz * dz > MAX_DIST_SQ) continue;

            addRectQuad(r, pose, vc);
        }
    }

    /**
     * Add one quad to the current buffer, lying on XZ plane at r.y.
     * Long side is aligned with yaw direction, short side is perpendicular.
     */
    private static void addRectQuad(RectOverlay r,
                                    PoseStack.Pose pose,
                                    VertexConsumer vc) {
        float cx = r.x;
        float cy = r.y;
        float cz = r.z;

        // Basis vectors on XZ plane
        float fx = r.cosYaw;  // forward (long side)
        float fz = r.sinYaw;

        float rx = -r.sinYaw; // right (short side)
        float rz = r.cosYaw;

        // Pre-multiply by half-lengths
        float oxF = fx * r.halfLong;
        float ozF = fz * r.halfLong;
        float oxR = rx * r.halfShort;
        float ozR = rz * r.halfShort;

        // 4 corners in world space (still on XZ plane at y=cy)
        float x0 = cx + oxF + oxR;
        float z0 = cz + ozF + ozR;

        float x1 = cx - oxF + oxR;
        float z1 = cz - ozF + ozR;

        float x2 = cx - oxF - oxR;
        float z2 = cz - ozF - ozR;

        float x3 = cx + oxF - oxR;
        float z3 = cz + ozF - ozR;

        float a = r.alpha;

        // Quad order: 0-1-2-3, using POSITION_COLOR format
        vc.addVertex(pose, x0, cy, z0).setColor(0f, 0f, 0f, a);
        vc.addVertex(pose, x1, cy, z1).setColor(0f, 0f, 0f, a);
        vc.addVertex(pose, x2, cy, z2).setColor(0f, 0f, 0f, a);
        vc.addVertex(pose, x3, cy, z3).setColor(0f, 0f, 0f, a);
    }
}
