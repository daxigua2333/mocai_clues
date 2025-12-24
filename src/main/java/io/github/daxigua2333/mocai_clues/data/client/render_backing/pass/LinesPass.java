//package io.github.daxigua2333.mocai_clues.data.client.render_backing.pass;
//
//import com.mojang.blaze3d.vertex.BufferBuilder;
//import com.mojang.blaze3d.vertex.DefaultVertexFormat;
//import com.mojang.blaze3d.vertex.VertexFormat;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.snapshot.ChunkSnapshot;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.snapshot.MyObjectSnapshot;
//
//public final class LinesPass implements CpuPass {
//
//    @Override public PassKind kind() { return PassKind.LINES; }
//    @Override public VertexFormat.Mode mode() { return VertexFormat.Mode.LINES; }
//    @Override public VertexFormat format() { return DefaultVertexFormat.POSITION_COLOR; }
//
//    @Override
//    public int estimateBytes(ChunkSnapshot snap) {
//        // 2 vertices per line segment * vertexSize; be conservative.
//        // POSITION_COLOR is 3 floats + 4 bytes -> alignment overhead exists; just approximate.
//        int v = snap.objs().length * 2;
//        return v * 32;
//    }
//
//    @Override
//    public void emit(BufferBuilder out, ChunkBuildContext ctx, ChunkSnapshot snap) {
//        int baseX = ctx.chunkMinX();
//        int baseY = ctx.chunkMinY();
//        int baseZ = ctx.chunkMinZ();
//
//        for (MyObjectSnapshot s : snap.objs()) {
//            float lx = (s.x() - baseX) + 0.5f;
//            float ly = (s.y() - baseY) + 0.5f;
//            float lz = (s.z() - baseZ) + 0.5f;
//
//            int c = s.argb();
//
//            // Example: vertical line of length "size"
//            float h = s.size();
//
//            out.addVertex(lx, ly, lz).setColor(c);
//            out.addVertex(lx, ly + h, lz).setColor(c);
//        }
//    }
//}
