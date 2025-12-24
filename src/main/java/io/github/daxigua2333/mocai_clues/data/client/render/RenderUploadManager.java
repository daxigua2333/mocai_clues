//package io.github.daxigua2333.mocai_clues.data.client.render;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.MeshData;
//import com.mojang.blaze3d.vertex.VertexBuffer;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.ChunkCpuBuildResult;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.OwnedMesh;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.pass.PassKind;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public final class RenderUploadManager {
//
//    private final Map<Long, ChunkRenderState> chunks =
//            new ConcurrentHashMap<>();
//
//    /** Render thread only */
//    public void consumeCpuResult(ChunkCpuBuildResult cpu) {
//        ChunkRenderState state = chunks.get(cpu.chunkKey());
//
//        // Replace if missing or revision changed
//        if (state == null || state.revision != cpu.revision()) {
//            if (state != null) {
//                state.close();
//            }
//            state = new ChunkRenderState(cpu.chunkKey(), cpu.revision());
//            chunks.put(cpu.chunkKey(), state);
//        }
//
//        for (var entry : cpu.meshes().entrySet()) {
//            PassKind kind = entry.getKey();
//            OwnedMesh owned = entry.getValue();
//            MeshData mesh = owned.mesh;
//
//            VertexBuffer vb = state.buffers.get(kind);
//            if (vb == null) {
//                vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
//                state.buffers.put(kind, vb);
//            }
//
//            // Upload to GPU
//            vb.bind();
//            vb.upload(mesh);
//            VertexBuffer.unbind();
//        }
//
//        // CPU memory is no longer needed
//        cpu.close();
//    }
//
//    public Map<Long, ChunkRenderState> chunkStates() {
//        return chunks;
//    }
//
//    /** Called on chunk unload / level unload */
//    public void removeChunk(long chunkKey) {
//        ChunkRenderState state = chunks.remove(chunkKey);
//        if (state != null) {
//            state.close();
//        }
//    }
//
//    public void clearAll() {
//        for (ChunkRenderState s : chunks.values()) {
//            s.close();
//        }
//        chunks.clear();
//    }
//}
