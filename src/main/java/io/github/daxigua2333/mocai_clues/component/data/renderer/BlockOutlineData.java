package io.github.daxigua2333.mocai_clues.component.data.renderer;

import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.system.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.data.BlockPosWithFace;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public class BlockOutlineData extends BaseRendererData {
    @Override
    public PassType getPassType() {
        return PassType.BLOCK_OUTLINE;
    }

    @Nullable
    @Override
    public ChunkPos getChunkPos(ClueObject owner) {
        // TODO: maybe other coordinate compo
        BlockPosWithFace bCompo = owner.getComponentOrThrow(ComponentType.BLOCK_POS_WITH_FACE);
        if (bCompo.getPos() == null) return null;
        return new ChunkPos(bCompo.getPos());
    }

    public static final MapCodec<BlockOutlineData> CODEC = MapCodec.unit(new BlockOutlineData());
}
