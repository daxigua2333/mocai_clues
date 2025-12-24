//package io.github.daxigua2333.mocai_clues.data.client.render;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexBuffer;
//import com.mojang.math.Axis;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.pass.PassKind;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.GameRenderer;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.culling.Frustum;
//import net.minecraft.world.level.ChunkPos;
//
//public final class WorldRenderer {
//
//    private final RenderUploadManager uploads;
//
//    public WorldRenderer(RenderUploadManager uploads) {
//        this.uploads = uploads;
//    }
//
//    public void render(
//            PoseStack poseStack,
//            Frustum frustum,
//            double camX, double camY, double camZ
//    ) {
//        var level = Minecraft.getInstance().level;
//        if (level == null) return;
//
//        for (ChunkRenderState state : uploads.chunkStates().values()) {
//            ChunkPos cp = new ChunkPos(state.chunkKey);
//
//            // Frustum cull (chunk AABB)
//            if (!frustum.isVisible(cp.getWorldBounds())) {
//                continue;
//            }
//
//            double dx = cp.getMinBlockX() - camX;
//            double dz = cp.getMinBlockZ() - camZ;
//
//            poseStack.pushPose();
//            poseStack.translate(dx, -camY, dz);
//
//            for (var entry : state.buffers.entrySet()) {
//                PassKind kind = entry.getKey();
//                var vb = entry.getValue();
//
//                RenderType rt = PassRenderTypes.typeFor(kind);  // TODO
//
//                vb.bind();
//                vb.drawWithShader(
//                        poseStack.last().pose(),
//                        RenderSystem.getProjectionMatrix(),
//                        GameRenderer.getPositionColorShader()
//                );
//            }
//
//            poseStack.popPose();
//        }
//
//        // Safety
//        VertexBuffer.unbind();
//    }
//}
