package io.github.daxigua2333.cmagic_clue.component.system.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.component.ComponentFamilyRegistry;
import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import io.github.daxigua2333.cmagic_clue.component.data.renderer.BaseRendererData;
import io.github.daxigua2333.cmagic_clue.component.data.renderer.RendererHolder;
import io.github.daxigua2333.cmagic_clue.component.system.renderer.pass.BasePass;
import io.github.daxigua2333.cmagic_clue.data.ObjectHolder;
import io.github.daxigua2333.cmagic_clue.data.ObjectHolderClientSyncedEvent;
import io.github.daxigua2333.cmagic_clue.data.common.IndexManager;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.ChunkPos;
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
public class ObjectRenderSystem {
    private static final ComponentFamilyRegistry.SystemFamily FAMILY = ComponentFamilyRegistry.SystemFamily.CHUNK_RENDER_SYSTEM;

    private static final Map<PassType, Set<ChunkPos>> DIRTY = new ConcurrentHashMap<>();
    private static final Map<PassType, Set<ChunkPos>> BUILDING = new ConcurrentHashMap<>();
    private static final Map<PassType, Map<ChunkPos, VertexBuffer>> BUFFERS = new ConcurrentHashMap<>();
    /* TODO: maybe optimize, type index in chunk attachment
     * Current structure treats per type per chunk as a batch, which means when building batch mesh,
     * each chunk data will be separately iterated by per type, which is unnecessarily repeated.
     * But for now, the PassType is like only O(10), so... whatever
     * */

    private static final int POOL_SIZE = 16;
    private static final BlockingQueue<ByteBufferBuilder> POOL = new ArrayBlockingQueue<>(POOL_SIZE);
    private static final int CAPACITY = 65536;

    // Call this from "onChange" sync hook
    public static void markDirty(PassType type, ChunkPos pos) {
        DIRTY.computeIfAbsent(type, key -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(pos);
    }

    @SubscribeEvent
    public static void onFullSync(ObjectHolderClientSyncedEvent.Full<ClueObject> event) {
        if (event.attachmentHolder instanceof LevelChunk chunk) {
            for (PassType pType : PassType.values()) {
                markDirty(pType, chunk.getPos());
            }
        } else if (event.attachmentHolder == null) {  // in SavedData
            for (ClueObject obj : event.full.values()) {
                markDirtyByObject(obj);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onDeltaSync(ObjectHolderClientSyncedEvent.Delta<ClueObject> event) {
        if (event.attachmentHolder instanceof LevelChunk chunk) {
            if (event.delta.cleared()) {
                var index = (ObjectHolder<ClueObject>.Index<PassType>) event.prev.getIndex(IndexManager.BY_PASS_TYPE);
                if (index != null) {
                    index.keySet().forEach(pType -> markDirty(pType, chunk.getPos()));
                }
            }
            for (ClueObject obj : event.delta.dirty()) {
                markDirtyByObjectAndChunkPos(obj, chunk.getPos());
            }
            for (UUID id : event.delta.removed()) {
                ClueObject obj = event.prev.get(id);
                markDirtyByObjectAndChunkPos(obj, chunk.getPos());
            }
        } else if (event.attachmentHolder == null) {
            if (event.delta.cleared()) {
                for (ClueObject obj : event.prev.values()) {
                    markDirtyByObject(obj);
                }
            }
            for (ClueObject obj : event.delta.dirty()) {
                markDirtyByObject(obj);
            }
            for (UUID id : event.delta.removed()) {
                ClueObject obj = event.prev.get(id);
                markDirtyByObject(obj);
            }

        }
    }

    private static void markDirtyByObjectAndChunkPos(ClueObject obj, ChunkPos chunkPos) {
        RendererHolder rCompo = obj.getComponent(ComponentType.RENDERER_HOLDER);
        if (rCompo == null) return;
        for (BaseRendererData pass : rCompo.getImmutable()) {
//            DIRTY.computeIfAbsent(pass.getPassType(), t -> new HashSet<>()).add(chunkPos);
            markDirty(pass.getPassType(), chunkPos);
        }
    }

    private static void markDirtyByObject(ClueObject obj) {
        // get types
        RendererHolder rCompo = obj.getComponent(ComponentType.RENDERER_HOLDER);
        if (rCompo != null) {
            for (BaseRendererData pass : rCompo.getImmutable()) {
                ChunkPos p = pass.getChunkPos(obj);
                if (p == null) continue;
                markDirty(pass.getPassType(), p);
            }
        }

    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
//        MoCaiClues.LOGGER.debug("tick verification: {}", DIRTY_CHUNKS);
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push(CMagicClue.MODID + ":render");

        processDirty();
        render(event);

        mc.getProfiler().pop();
    }

    // ======== LifeCycle ==========
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            ChunkPos pos = event.getChunk().getPos();
            // Remove from dirty/building queues
            for (PassType type : PassType.values()) {
                if (DIRTY.containsKey(type)) DIRTY.get(type).remove(pos);
                // Note: If it's currently BUILDING, the async task will finish,
                // but uploadMesh checks if the chunk is still valid or we can simple let it upload and it gets cleaned next frame.
                // However, we must ensure the VBO is destroyed.
                Map<ChunkPos, VertexBuffer> map = BUFFERS.get(type);
                if (map != null) {
                    VertexBuffer vbo = map.remove(pos);
                    if (vbo != null) vbo.close();
                }
            }
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
        // close all vbo
        BUFFERS.forEach((k, v) -> v.values().forEach(VertexBuffer::close));
        BUFFERS.clear();
        // close all bbb
//        for (int i = 0; i < POOL.size(); i++) {
//            try {
//                ByteBufferBuilder bbb = POOL.take();
//                bbb.close();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
        List<ByteBufferBuilder> builders = new ArrayList<>();
        POOL.drainTo(builders);
        builders.forEach(ByteBufferBuilder::close);
        // close others
        BUILDING.clear();
        DIRTY.clear();

    }


    private static void processDirty() {
        for (var entry : DIRTY.entrySet()) {
            PassType pType = entry.getKey();
            Set<ChunkPos> dirtyChunks = entry.getValue();

            if (dirtyChunks.isEmpty()) continue;

            Iterator<ChunkPos> iterator = dirtyChunks.iterator();
            while (iterator.hasNext()) {
                ChunkPos chunkPos = iterator.next();
                if (BUILDING.containsKey(pType) && BUILDING.get(pType).contains(chunkPos)) continue;

                BUILDING.computeIfAbsent(pType, key -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(chunkPos);
                iterator.remove();

                // build batch mesh off main thread
                CompletableFuture.supplyAsync(() -> buildBatchMesh(pType, chunkPos), Util.backgroundExecutor())
                        .whenCompleteAsync(
                                ((pending, throwable) -> uploadMesh(chunkPos, pType, pending, throwable)),
                                Minecraft.getInstance());

            }
        }
    }

    record PendingMesh(@Nullable ByteBufferBuilder builder, @Nullable MeshData mesh) {
    }

    private static PendingMesh buildBatchMesh(PassType pType, ChunkPos chunkPos) {
        MeshData result = null;
        ByteBufferBuilder bbb = null;

        try {
            bbb = POOL.take();

            // get BufferBuilder
            BasePass pass = pType.getPass();
            RenderType rType = pass.getRenderType();
            if (rType == null) throw new RuntimeException("Unregistered render type for pass type: " + pType);
            BufferBuilder builder = new BufferBuilder(bbb, rType.mode(), rType.format());

            // TODO: Coordinates should be relative to Chunk (0-15)
            // to prevent floating point jitter at high coordinates
            pass.addToMesh(builder, chunkPos);

            // Finalize meshes
//            result = builder.buildOrThrow();  // this shit only add a null check to #build()....
            result = builder.build();
            return new PendingMesh(bbb, result);

        } catch (Throwable t) {
            if (result != null) result.close();
//            throw new RuntimeException(t);
            CMagicClue.LOGGER.error("Off main thread building mesh fails in batch: PassType#{}, ChunkPos#{}", pType, chunkPos, t);
            return new PendingMesh(bbb, null);
        } finally {
//            pool.close();
        }
    }

    private static void uploadMesh(ChunkPos chunkPos, PassType pType, PendingMesh pending, Throwable throwable) {
        // This block runs regardless of success or failure
        MeshData mesh = pending.mesh;
        try {
            if (throwable != null) {
                CMagicClue.LOGGER.error("Failed to build batch mesh of (chunk: {}, passType: {})", chunkPos, pType, throwable);
            } else {
                if (mesh != null) { // Success: Upload the mesh
                    VertexBuffer vbo = BUFFERS.computeIfAbsent(pType, k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(chunkPos, k -> new VertexBuffer(VertexBuffer.Usage.DYNAMIC));
                    vbo.bind();
                    vbo.upload(mesh);
                    VertexBuffer.unbind();
                } else {  // If empty mesh, close(clear) it
                    Map<ChunkPos, VertexBuffer> map = BUFFERS.get(pType);
                    if (map != null) {
                        VertexBuffer vbo = map.remove(chunkPos);
                        if (vbo != null) {
                            vbo.close();
                        }
                    }
                }
            }
        } finally {
            // ALWAYS remove from the building set, even if it crashed
            BUILDING.get(pType).remove(chunkPos);
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
        // Translate to chunk origin relative to camera
//        poseStack.translate(pos.getMinBlockX() - camPos.x, -camPos.y, pos.getMinBlockZ() - camPos.z);
//        poseStack.mulPose(new Quaternionf(camera.rotation()).conjugate());
        poseStack.mulPose(event.getModelViewMatrix());
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

//        Frustum frustum = new Frustum(poseStack.last().pose(), projection);
//        frustum.prepare(camPos.x, camPos.y, camPos.z);


        /*
        * for each RenderType layer:
              setup layer state once
              for each visible section that has that layer:
                bind that section’s VBO
                draw
              clear layer state
        * */
        for (var e1 : BUFFERS.entrySet()) {
            PassType pType = e1.getKey();

            BasePass pass = pType.getPass();
            if (!pass.doRender(Minecraft.getInstance())) continue;

            RenderType rType = pass.getRenderType();
            if (rType == null) throw new RuntimeException("Unregistered render type for pass component type: " + pType);

            rType.setupRenderState();
            pass.setupRenderState();


            for (var e2 : e1.getValue().entrySet()) {
                ChunkPos pos = e2.getKey();
                VertexBuffer vbo = e2.getValue();

                // Frustum Culling
                // TODO: distance culling (now all synced data are rendered)
                if (!frustum.isVisible(new AABB(pos.getMinBlockX(), -64, pos.getMinBlockZ(), pos.getMaxBlockX(), 320, pos.getMaxBlockZ()))) {
                    continue;
                }
                if (vbo == null || vbo.isInvalid()) continue;
//                if (vbo == null) continue;

                vbo.bind();
//                vbo.drawWithShader(poseStack.last().pose(), projection, pass.getShader());
                vbo.drawWithShader(poseStack.last().pose(), projection, RenderSystem.getShader());
                VertexBuffer.unbind();

            }

            pass.clearRenderState();
            rType.clearRenderState();
        }

        poseStack.popPose();
    }

}
