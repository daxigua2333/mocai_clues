package io.github.daxigua2333.cmagic_clue.decal.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.data.ModAttachmentRegistry;
import io.github.daxigua2333.cmagic_clue.decal.DecalLayerHolder;
import io.github.daxigua2333.cmagic_clue.decal.common.DecalMisc;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(value = Dist.CLIENT)
@EventBusSubscriber(modid = CMagicClue.MODID, value = Dist.CLIENT)
public class DecalRenderer {
    private static final Set<ChunkPos> DIRTY = ConcurrentHashMap.newKeySet();
    private static final Set<ChunkPos> BUILDING = ConcurrentHashMap.newKeySet();
    private static final Map<ChunkPos, VertexBuffer> BUFFERS = new ConcurrentHashMap<>();

    private static final int POOL_SIZE = 8;
    private static final BlockingQueue<ByteBufferBuilder> POOL = new ArrayBlockingQueue<>(POOL_SIZE);
    private static final int CAPACITY = 65536 / 2;


    public static void markDirty(ChunkPos pos) {
        DIRTY.add(pos);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push(CMagicClue.MODID + ":decal_render");

        processDirty();
        render(event);

        mc.getProfiler().pop();
    }

    // ======== LifeCycle ==========
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ChunkPos pos = event.getChunk().getPos();

            DIRTY.remove(pos);
            VertexBuffer vbo = BUFFERS.remove(pos);
            if (vbo != null) vbo.close();

            DecalLayerHolder holder = event.getChunk().getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER);
            holder.closeDynamic(DecalAtlasRegistry.ATLAS);
        }
    }

    // avoid leaking GPU buffers when leaving a world/server
    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        cleanup();
    }

    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        cleanup();
        for (int i = 0; i < POOL_SIZE; i++) {
            POOL.add(new ByteBufferBuilder(CAPACITY));
        }
    }

    private static void cleanup() {
        // close all vbo and its allocated texture
        BUFFERS.values().forEach(VertexBuffer::close);
        BUFFERS.keySet().forEach(chunkPos -> {
            LevelChunk chunk = Minecraft.getInstance().level.getChunk(chunkPos.x, chunkPos.z);
            chunk.getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER).closeDynamic(DecalAtlasRegistry.ATLAS);
        });
        BUFFERS.clear();
        // close all bbb
        List<ByteBufferBuilder> builders = new ArrayList<>();
        POOL.drainTo(builders);
        builders.forEach(ByteBufferBuilder::close);
        // close others
        BUILDING.clear();
        DIRTY.clear();
    }

    private static void processDirty() {
        if (DIRTY.isEmpty()) return;

        Iterator<ChunkPos> iterator = DIRTY.iterator();
        while (iterator.hasNext()) {
            ChunkPos chunkPos = iterator.next();
            if (BUILDING.contains(chunkPos)) continue;

            BUILDING.add(chunkPos);
            iterator.remove();

            CompletableFuture.supplyAsync(() -> buildChunkMesh(chunkPos), Util.backgroundExecutor())
                    .whenCompleteAsync(
                            ((pending, throwable) -> uploadMesh(chunkPos, pending, throwable)),
                            Minecraft.getInstance());
        }
    }

    record PendingMesh(@Nullable ByteBufferBuilder builder, @Nullable MeshData mesh) {
    }

    public static final RenderType DECAL_RENDER_TYPE = RenderType.create(
            CMagicClue.MODID + ":decal",
            DefaultVertexFormat.POSITION_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,  // no sort
            RenderType.CompositeState.builder()
                    // Any textured translucent shader is fine; this one is commonly used.
                    .setShaderState(RenderStateShard.POSITION_TEX_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(DecalAtlasRegistry.ATLAS_ID, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    // Keep depth test (so it can be occluded), but disable depth *write* to avoid z-fighting.
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE) // <- no depth write
                    .setCullState(RenderStateShard.NO_CULL)
                    // Optional, but helps when coplanar with block faces.
//                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setLayeringState(RenderStateShard.NO_LAYERING)
                    .createCompositeState(false)
    );


    private static PendingMesh buildChunkMesh(ChunkPos chunkPos) {
        MeshData result = null;
        ByteBufferBuilder bbb = null;

        try {
            bbb = POOL.take();
            RenderType rType = DECAL_RENDER_TYPE;
            BufferBuilder builder = new BufferBuilder(bbb, rType.mode(), rType.format());

            // build mesh
            Level level = Minecraft.getInstance().level;
            if (level == null) throw new RuntimeException();
            LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
            DecalLayerHolder holder = chunk.getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER);
            holder.addToMesh(builder, DecalMisc.LAYER_FLOAT_OFFSET);

            // Finalize meshes
//            result = builder.buildOrThrow();  // this shit only add a null check to #build()....
            result = builder.build();
            return new PendingMesh(bbb, result);
        } catch (Throwable t) {
            if (result != null) result.close();
//            throw new RuntimeException(t);
            CMagicClue.LOGGER.error("Off main thread building DECAL mesh fails in batch, ChunkPos#{}", chunkPos, t);
            return new PendingMesh(bbb, null);
        } finally {
//            pool.close();
        }

    }

    private static void uploadMesh(ChunkPos chunkPos, PendingMesh pending, Throwable throwable) {
        // This block runs regardless of success or failure
        MeshData mesh = pending.mesh;
        try {
            if (throwable != null) {
                CMagicClue.LOGGER.error("Failed to build DECAL mesh of (chunk: {}})", chunkPos, throwable);
            } else {
                if (mesh != null) { // Success: Upload the mesh
                    VertexBuffer vbo = BUFFERS.computeIfAbsent(chunkPos, k -> new VertexBuffer(VertexBuffer.Usage.DYNAMIC));
                    vbo.bind();
                    vbo.upload(mesh);
                    VertexBuffer.unbind();
                } else {  // If empty mesh, close(clear) it
                    VertexBuffer vbo = BUFFERS.remove(chunkPos);
                    if (vbo != null) {
                        vbo.close();
                    }
                }
            }
        } finally {
            // ALWAYS remove from the building set, even if it crashed
            BUILDING.remove(chunkPos);
            if (mesh != null) {
                mesh.close();
            }
            if (pending.builder != null) {
                POOL.offer(pending.builder);
            }
        }

    }


    private static void render(RenderLevelStageEvent event) {
        PoseStack poseStack = event.getPoseStack();
        Matrix4f projection = event.getProjectionMatrix();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        Frustum frustum = event.getFrustum();

        poseStack.pushPose();
        poseStack.mulPose(event.getModelViewMatrix());
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        DECAL_RENDER_TYPE.setupRenderState();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        for (var entry : BUFFERS.entrySet()) {

            ChunkPos pos = entry.getKey();
            VertexBuffer vbo = entry.getValue();

            if (!frustum.isVisible(new AABB(pos.getMinBlockX(), -64, pos.getMinBlockZ(), pos.getMaxBlockX(), 320, pos.getMaxBlockZ()))) {
                continue;
            }
            if (vbo == null || vbo.isInvalid()) continue;

            vbo.bind();
            vbo.drawWithShader(poseStack.last().pose(), projection, RenderSystem.getShader());
            VertexBuffer.unbind();
        }

        RenderSystem.depthMask(true);
        DECAL_RENDER_TYPE.clearRenderState();

        poseStack.popPose();
    }


}
