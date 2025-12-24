package io.github.daxigua2333.mocai_clues.data.client.render_backing.pass;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.daxigua2333.mocai_clues.data.client.render_backing.snapshot.ChunkSnapshot;

public interface CpuPass {
    PassKind kind();

    VertexFormat.Mode mode();
    VertexFormat format(); // usually DefaultVertexFormat.*

    /** Conservative bytes estimate to size ByteBufferBuilder */
    int estimateBytes(ChunkSnapshot snap);

    /** Emit vertices in chunk-local space (0..16, relative to chunk origin). */
    void emit(BufferBuilder out, ChunkBuildContext ctx, ChunkSnapshot snap);
}