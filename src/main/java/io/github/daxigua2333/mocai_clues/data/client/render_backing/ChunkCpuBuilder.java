//package io.github.daxigua2333.mocai_clues.data.client.render_backing;
//
//import com.mojang.blaze3d.vertex.BufferBuilder;
//import com.mojang.blaze3d.vertex.ByteBufferBuilder;
//import com.mojang.blaze3d.vertex.MeshData;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.pass.ChunkBuildContext;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.pass.CpuPass;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.pass.PassKind;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.snapshot.ChunkSnapshot;
//import net.minecraft.world.level.ChunkPos;
//
//import java.util.EnumMap;
//import java.util.List;
//
//public final class ChunkCpuBuilder {
//
//    private final List<CpuPass> passes;
//
//    public ChunkCpuBuilder(List<CpuPass> passes) {
//        this.passes = List.copyOf(passes);
//    }
//
//    public ChunkCpuBuildResult build(ChunkPos cp, ChunkSnapshot snap) {
//        int chunkMinX = cp.getMinBlockX();
//        int chunkMinZ = cp.getMinBlockZ();
//        // If you want true chunk-local Y, set minY to 0; or section base if you later split by section.
//        int chunkMinY = 0;
//
//        ChunkBuildContext ctx = new ChunkBuildContext(chunkMinX, chunkMinY, chunkMinZ);
//
//        EnumMap<PassKind, OwnedMesh> out = new EnumMap<>(PassKind.class);
//
//        for (CpuPass pass : passes) {
//            int bytes = Math.max(256, pass.estimateBytes(snap));
//
//            // ByteBufferBuilder(int capacity) exists and is off-heap
//            ByteBufferBuilder bbb = new ByteBufferBuilder(bytes);
//
//            // BufferBuilder(ByteBufferBuilder, VertexFormat.Mode, VertexFormat) exists
//            BufferBuilder bb = new BufferBuilder(bbb, pass.mode(), pass.format());
//
//            pass.emit(bb, ctx, snap);
//
//            // buildOrThrow produces MeshData
//            MeshData mesh = bb.buildOrThrow();
//
//            out.put(pass.kind(), new OwnedMesh(mesh, bbb));
//        }
//
//        return new ChunkCpuBuildResult(snap.chunkKey(), snap.revision(), out);
//    }
//}
