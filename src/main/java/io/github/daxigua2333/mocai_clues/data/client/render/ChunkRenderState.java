//package io.github.daxigua2333.mocai_clues.data.client.render;
//
//import com.mojang.blaze3d.vertex.VertexBuffer;
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.pass.PassKind;
//
//import java.util.EnumMap;
//
//public final class ChunkRenderState implements AutoCloseable {
//    public final long chunkKey;
//    public int revision;
//
//    // One VBO per pass
//    public final EnumMap<PassKind, VertexBuffer> buffers =
//            new EnumMap<>(PassKind.class);
//
//    public ChunkRenderState(long chunkKey, int revision) {
//        this.chunkKey = chunkKey;
//        this.revision = revision;
//    }
//
//    @Override
//    public void close() {
//        for (VertexBuffer vb : buffers.values()) {
//            vb.close();
//        }
//        buffers.clear();
//    }
//}
