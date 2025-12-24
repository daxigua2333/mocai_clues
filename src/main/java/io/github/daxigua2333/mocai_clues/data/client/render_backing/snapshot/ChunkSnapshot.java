package io.github.daxigua2333.mocai_clues.data.client.render_backing.snapshot;

public record ChunkSnapshot(
        long chunkKey,            // ChunkPos.asLong(x,z)
        int revision,
        MyObjectSnapshot[] objs   // could be primitive arrays if needed later
) {}
