package io.github.daxigua2333.mocai_clues.entry.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.component.world.renderer.data.BaseRendererData;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BasePass;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderClientSyncedEvent;
import io.github.daxigua2333.mocai_clues.data.client.ClientIndexManager;
import io.github.daxigua2333.mocai_clues.data.common.IndexManager;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(value = Dist.CLIENT)
@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public class ObjectRenderSystem {
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
                ObjectHolder<ClueObject> holder = event.prev;
                var index = (ObjectHolder<ClueObject>.Index<PassType>) holder.getIndex(IndexManager.BY_PASS_TYPE);
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
        Set<PassType> types = new HashSet<>();
        RendererHolder rCompo = obj.getComponent(ComponentType.RENDERER_HOLDER);
        if (rCompo == null) return;
        for (BaseRendererData pass : rCompo.getImmutable()) {
            types.add(pass.getPassType());
        }

        // get chunks
        Set<ChunkPos> chunks = new HashSet<>();
        BlockPosSet pCompo = obj.getComponent(ComponentType.BLOCK_POS_SET);
        if(pCompo == null) return;
        for (BlockPos pos : pCompo.getImmutable()) {
            chunks.add(new ChunkPos(pos));
        }

        // mark dirty per chunk per type
        for (var type : types) {
            for (var chunk : chunks) {
                markDirty(type, chunk);
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
//        MoCaiClues.LOGGER.debug("tick verification: {}", DIRTY_CHUNKS);
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        mc.getProfiler().push("mocai_clues:render");

        processDirty();
        render(event);

        mc.getProfiler().pop();
    }

    // avoid leaking GPU buffers when leaving a world/server
    @SubscribeEvent
    public static void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
        // close all vbo
        BUFFERS.forEach((k,v) -> v.values().forEach(VertexBuffer::close));
        BUFFERS.clear();
        // close all bbb
        for (int i=0; i<POOL.size(); i++) {
            try {
                ByteBufferBuilder bbb = POOL.take();
                bbb.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // close others
        BUILDING.clear();
        DIRTY.clear();
    }

    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        for (int i=POOL.size(); i<POOL_SIZE; i++) {
            POOL.add(new ByteBufferBuilder(CAPACITY));
        }
    }

    private static void processDirty() {
        for (var entry : DIRTY.entrySet()) {
            PassType pType = entry.getKey();
            Set<ChunkPos> dirty_chunks = entry.getValue();

            if (dirty_chunks.isEmpty()) continue;

            Iterator<ChunkPos> iterator = dirty_chunks.iterator();
            while(iterator.hasNext()) {
                ChunkPos chunkPos = iterator.next();
                if (BUILDING.containsKey(pType) && BUILDING.get(pType).contains(chunkPos)) continue;

                BUILDING.computeIfAbsent(pType, key -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(chunkPos);
                iterator.remove();

                // build batch mesh off main thread
                CompletableFuture.supplyAsync(() -> buildBatchMesh(pType, chunkPos), Util.backgroundExecutor())
                    .whenCompleteAsync(((pending, throwable) -> uploadMesh(chunkPos, pType, pending, throwable)), Minecraft.getInstance());

            }
        }
    }

    record PendingMesh(@Nullable ByteBufferBuilder  builder, @Nullable MeshData mesh) {}
    private static PendingMesh buildBatchMesh(PassType pType, ChunkPos chunkPos) {
        MeshData result = null;
        ByteBufferBuilder bbb = null;

        try {
            bbb = POOL.take();

            // get BufferBuilder
            BasePass pass = PassType.getPass(pType);
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
            MoCaiClues.LOGGER.error("Off main thread building mesh fails in batch: PassType#{}, ChunkPos#{}", pType, chunkPos, t);
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
                MoCaiClues.LOGGER.error("Failed to build batch mesh of (chunk: {}, passType: {})", chunkPos, pType, throwable);
            } else {
                if (mesh != null) { // Success: Upload the mesh
                    VertexBuffer vbo = BUFFERS.computeIfAbsent(pType, k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(chunkPos, k -> new VertexBuffer(VertexBuffer.Usage.DYNAMIC));
                    vbo.bind();
                    vbo.upload(mesh);
                    VertexBuffer.unbind();
                } else {  // If empty mesh, close(clear) it
                    VertexBuffer vbo = BUFFERS.getOrDefault(pType, new HashMap<>()).remove(chunkPos);
                    if (vbo != null) {
                        vbo.close();
                    }
                }
            }
        } finally {
            // ALWAYS remove from the building set, even if it crashed
            BUILDING.get(pType).remove(chunkPos);
            if (pending.builder != null) {
                POOL.offer(pending.builder);
            }
        }
    }

    private static void render(RenderLevelStageEvent event) {
        PoseStack poseStack = event.getPoseStack();
        Matrix4f projection = event.getProjectionMatrix();
        Camera camera = event.getCamera();
        Frustum frustum = event.getFrustum();

        poseStack.pushPose();
        Vec3 camPos = camera.getPosition();
        // Translate to chunk origin relative to camera
//        poseStack.translate(pos.getMinBlockX() - camPos.x, -camPos.y, pos.getMinBlockZ() - camPos.z);
        poseStack.mulPose(new Quaternionf(camera.rotation()).conjugate());
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

            BasePass pass = PassType.getPass(pType);
            if (! pass.doRender(Minecraft.getInstance())) continue;

            RenderType rType = pass.getRenderType();
            if (rType == null) throw new RuntimeException("Unregistered render type for pass component type: " + pType);

            rType.setupRenderState();
            pass.setupRenderState();


            for (var e2: e1.getValue().entrySet()) {
                ChunkPos pos = e2.getKey();
                VertexBuffer vbo = e2.getValue();

                // Frustum Culling
                // TODO: distance culling
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
