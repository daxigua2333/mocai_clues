package io.github.daxigua2333.mocai_clues.entry.client.render;

import com.mojang.blaze3d.vertex.*;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BasePass;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderClientSyncedEvent;
import io.github.daxigua2333.mocai_clues.data.client.api.ClientAccessor;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public class ObjectRenderManager {
    private static final Map<ChunkPos, ChunkRenderBatch> CHUNK_BATCHES = new ConcurrentHashMap<>();
    private static final Set<ChunkPos> DIRTY_CHUNKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Set<ChunkPos> BUILDING_CHUNKS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final int POOL_SIZE = 10;
    private static final BlockingQueue<ByteBufferBuilder> POOL = new ArrayBlockingQueue<>(POOL_SIZE);
    private static final int CAPACITY = 65536;
    static {
        for (int i=0; i<POOL_SIZE; i++) {
            POOL.add(new ByteBufferBuilder(CAPACITY));
        }
    }

    // Call this from "onChange" sync hook
    public static void markChunkDirty(ChunkPos pos) {
        DIRTY_CHUNKS.add(pos);
    }
    @SubscribeEvent
    public static void onDataSyncChange(ObjectHolderClientSyncedEvent.Post event) {
        if (event.getAttachmentHolder() instanceof LevelChunk chunk) {
            ObjectRenderManager.markChunkDirty(chunk.getPos());
        } else if (event.getAttachmentHolder() == null) {  // TODO: event records deltas
            ClientAccessor.queryChunkPosInSavedData().forEach(pos -> ObjectRenderManager.markChunkDirty(pos));
        }
    }


    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
//        MoCaiClues.LOGGER.debug("tick verification: {}", DIRTY_CHUNKS);
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        processDirtyChunks();
        renderBatches(event);
    }

    // avoid leaking GPU buffers when leaving a world/server
    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        CHUNK_BATCHES.values().forEach(ChunkRenderBatch::close);
        for (int i=0; i<POOL_SIZE; i++) {
            try {
                ByteBufferBuilder bbb = POOL.take();
                bbb.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void processDirtyChunks() {
        if (DIRTY_CHUNKS.isEmpty()) return;

        Iterator<ChunkPos> iterator = DIRTY_CHUNKS.iterator();
        while (iterator.hasNext()) {
            ChunkPos pos = iterator.next();
            if (BUILDING_CHUNKS.contains(pos)) continue;

            BUILDING_CHUNKS.add(pos);
            iterator.remove();

            // Off-thread mesh building
            CompletableFuture.supplyAsync(() -> buildChunkMesh(pos), Util.backgroundExecutor())
                .whenCompleteAsync((pending, throwable) -> {
                    // This block runs regardless of success or failure
                    var meshMap = pending.meshMap;
                    try {
                        if (throwable != null) {
                            MoCaiClues.LOGGER.error("Failed to build chunk mesh at " + pos, throwable);
                        } else if (meshMap != null) {
                            // Success: Upload the mesh
                            ChunkRenderBatch batch = CHUNK_BATCHES.computeIfAbsent(pos, p -> new ChunkRenderBatch());
                            batch.upload(meshMap);  // TODO: it is said that upload api would close MeshData
                        }
                    } finally {
                        // ALWAYS remove from the building set, even if it crashed
                        BUILDING_CHUNKS.remove(pos);
                        POOL.offer(pending.builder);
                    }
                }, Minecraft.getInstance());
        }
    }

    record PendingMesh(ByteBufferBuilder builder, Map<ComponentType, MeshData> meshMap) {}
    private static PendingMesh buildChunkMesh(ChunkPos chunkPos) {

        // 1. Get your data from Chunk Attachment
        Level level = Minecraft.getInstance().level;
//        LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
//        var dataMap = chunk.getData(MyAttachments.CHUNK_DATA_MAP);
        List<ClueObject> data = ClientAccessor.queryClueObjectByChunkPos(chunkPos);  // TODO;

        Map<ComponentType, BufferBuilder> builders = new EnumMap<>(ComponentType.class);
        Map<ComponentType, MeshData> result = new EnumMap<>(ComponentType.class);
        // because it's async, so cannot reuse same buffer. must one buffer per worker
//        int perSize = DefaultVertexFormat.POSITION_COLOR_NORMAL.getVertexSize();
//        ByteBufferBuilder pool = new ByteBufferBuilder(data.size() * 64 * perSize);

        try {
            ByteBufferBuilder bbb = POOL.take();
            data.forEach(obj -> {
                for (ClueComponent compo : obj.getComponents()) {
                    if (compo instanceof BasePass passCompo) {
                        ComponentType type = passCompo.type();
                        RenderType rType = passCompo.renderType();
                        BufferBuilder builder = builders.computeIfAbsent(type, t -> new BufferBuilder(bbb, rType.mode(), rType.format()));

                        // Build the geometry (Lines, Quads, etc.)
                        // TODO: Coordinates should be relative to Chunk (0-15)
                        // to prevent floating point jitter at high coordinates
                        passCompo.addToMesh(builder);
                    }
                }
            });
            // Finalize meshes
            builders.forEach((type, buf) -> result.put(type, buf.buildOrThrow()));
            return new PendingMesh(bbb, result);

        } catch (Throwable t) {
            result.values().forEach(MeshData::close);
            throw new RuntimeException(t);
        } finally {
//            pool.close();
        }
    }

    private static void renderBatches(RenderLevelStageEvent event) {
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

        for (var entry : CHUNK_BATCHES.entrySet()) {
            ChunkPos pos = entry.getKey();
            ChunkRenderBatch batch = entry.getValue();

            // Frustum Culling
            if (!frustum.isVisible(new AABB(pos.getMinBlockX(), -64, pos.getMinBlockZ(), pos.getMaxBlockX(), 320, pos.getMaxBlockZ()))) {
                continue;
            }
            // TODO: distance culling

            // Render each type
            batch.renderAllTypes(poseStack, projection);
        }
        poseStack.popPose();
    }
}
