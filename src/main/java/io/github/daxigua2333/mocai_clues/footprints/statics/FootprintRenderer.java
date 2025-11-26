package io.github.daxigua2333.mocai_clues.footprints.statics;

import com.mojang.blaze3d.vertex.*;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.footprints.data.Footprint;
import io.github.daxigua2333.mocai_clues.footprints.ModFootprintRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public class FootprintRenderer {
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            renderFootprints(event);  // TODO: event.getPartialTick()
        }
    }

    private static void renderFootprints(RenderLevelStageEvent evt) {
        PoseStack poseStack = evt.getPoseStack();
//        Matrix4f modelView = evt.getModelViewMatrix();
        MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        Iterable<Footprint> list = collectVisibleChunkData(evt);

        // Push a matrix to translate to camera space
        poseStack.pushPose();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

        VertexConsumer vc = bufferSource.getBuffer(ModFootprintRegistry.FOOTPRINT_RENDER_TYPE);

//        PoseStack.Pose pose = poseStack.last();
        PoseStack.Pose modelView = poseStack.last();

        for (Footprint fp : list) {
            // Cull based on distance (optional)
            double dx = fp.x() - cameraPos.x();
            double dz = fp.z() - cameraPos.z();
            if (dx * dx + dz * dz > 256 * 256) { // e.g., only within 256 blocks
                continue;
            }

            // Compute quad corners in world space
            float halfLong = fp.longSide() * 0.5f;
            float halfShort = fp.shortSide() * 0.5f;

            // Rotation around Y axis
            double rad = Math.toRadians(fp.rotation());
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);

            // Local axes
            double lx = cos * halfLong;
            double lz = sin * halfLong;
            double sx = -sin * halfShort;
            double sz = cos * halfShort;

            // Four corners (world)
            double x0 = fp.x() - lx - sx;
            double z0 = fp.z() - lz - sz;
            double x1 = fp.x() + lx - sx;
            double z1 = fp.z() + lz - sz;
            double x2 = fp.x() + lx + sx;
            double z2 = fp.z() + lz + sz;
            double x3 = fp.x() - lx + sx;
            double z3 = fp.z() - lz + sz;

            float y = (float) fp.y();

            int alphaInt = (int)(fp.alpha() * 255f);
            int r = 0, g = 0, b = 0;

            vc.addVertex(modelView, (float)x0, y, (float)z0).setColor(r, g, b, alphaInt);
            vc.addVertex(modelView, (float)x1, y, (float)z1).setColor(r, g, b, alphaInt);
            vc.addVertex(modelView, (float)x2, y, (float)z2).setColor(r, g, b, alphaInt);
            vc.addVertex(modelView, (float)x3, y, (float)z3).setColor(r, g, b, alphaInt);
        }

//        bufferSource.endBatch(ModRenderTypes.FOOTPRINT);
//        BufferUploader.drawWithShader(buffer.buildOrThrow());


        poseStack.popPose();
    }

    private static Iterable<Footprint> collectVisibleChunkData(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return Collections.emptyList();
        }

        // Chunk source = ClientChunkCache
        ClientChunkCache chunkSource = level.getChunkSource();
        // Camera + frustum
        var camera = event.getCamera();
        Frustum frustum = event.getFrustum();
        // Effective render distance in chunks (client/server combined)
        int renderDistance = mc.options.getEffectiveRenderDistance();
        // Center chunk around the camera
        var camPos = camera.getBlockPosition();
        int centerChunkX = SectionPos.blockToSectionCoord(camPos.getX());
        int centerChunkZ = SectionPos.blockToSectionCoord(camPos.getZ());
        // Radius a little larger than render distance, to be safe
        int radius = renderDistance + 1;

        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight();

        Collection<Footprint> result = new ArrayList<>();
        // Optional: avoid double-processing chunks if you ever change the loop layout
//        LongOpenHashSet seen = new LongOpenHashSet();

        double camX = camera.getPosition().x;
        double camZ = camera.getPosition().z;
        double maxDistSq = Math.pow((renderDistance + 1) * 16.0, 2.0);

        for (int dz = -radius; dz <= radius; ++dz) {
            int chunkZ = centerChunkZ + dz;
            for (int dx = -radius; dx <= radius; ++dx) {
                int chunkX = centerChunkX + dx;

                long key = ChunkPos.asLong(chunkX, chunkZ);
//                if (!seen.add(key)) continue;

                // Only consider currently loaded chunks
                LevelChunk chunk = chunkSource.getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;

                // Rough 2D distance check first (cheap)
                double centerX = (chunkX << 4) + 8.0;
                double centerZ = (chunkZ << 4) + 8.0;
                double dxWorld = centerX - camX;
                double dzWorld = centerZ - camZ;
                if ((dxWorld * dxWorld + dzWorld * dzWorld) > maxDistSq) {
                    continue;
                }

                // Build AABB for this chunk & frustum-cull it
                AABB aabb = new AABB(
                        (chunkX << 4),      minY,
                        (chunkZ << 4),
                        (chunkX << 4) + 16, maxY,
                        (chunkZ << 4) + 16
                );

                if (!frustum.isVisible(aabb)) {
                    continue;
                }

                // Finally, get your attachment (using getExistingData to avoid instantiating defaults)
                result.addAll(FootprintClientHelper.getAllByChunk(chunk));
            }
        }

        return result;
    }

}
