package io.github.daxigua2333.mocai_clues.component.world.data;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** in chunk attachment, this exists only once
 *  but in SavedData, this can exist multiply */
public class WorldBlockPos extends ClueComponent {
    private long pos;
    private long getPos() {
        return pos;
    }
    private void setPos(BlockPos pos) {
        this.pos = pos.asLong();
    }
    public BlockPos getBlockPos() {
        return new BlockPos(BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos));
    }

    public WorldBlockPos(BlockPos pos) {
        this.setPos(pos);
    }
    public WorldBlockPos(long pos) {
        this.pos = pos;
    }

    @Override
    public ComponentType type() {
        return ComponentType.WORLD_BLOCK_POS;
    }

    public static Codec<WorldBlockPos> CODEC = Codec.LONG.xmap(WorldBlockPos::new, WorldBlockPos::getPos);

    @Nullable
    @Override
    public List<AbstractWidget> getEditable() {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of();
    }
}
