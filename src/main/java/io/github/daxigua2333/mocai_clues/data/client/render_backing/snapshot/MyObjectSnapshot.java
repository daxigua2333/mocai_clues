package io.github.daxigua2333.mocai_clues.data.client.render_backing.snapshot;

import net.minecraft.core.BlockPos;

public record MyObjectSnapshot(
        long packedPos,      // BlockPos.asLong()
        int argb,            // packed color (or -1)
        byte kindFlags,      // arbitrary; encode “type”, “state”, etc
        float size           // example
) {
    public int x() { return BlockPos.getX(packedPos); }
    public int y() { return BlockPos.getY(packedPos); }
    public int z() { return BlockPos.getZ(packedPos); }
}

