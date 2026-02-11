package io.github.daxigua2333.mocai_clues.component.world.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockPosWithFace extends ClueComponent {
    @Nullable
    private BlockPos pos;
    @Nullable
    private Direction face;

    private BlockPosWithFace(@Nullable BlockPos pos, @Nullable Direction face) {
        this.pos = pos;
        this.face = face;
    }

    public BlockPosWithFace() {
        this(null, null);
    }

    public @Nullable BlockPos getPos() {
        return pos;
    }

    public @Nullable Direction getFace() {
        return face;
    }

    @Override
    public ComponentType type() {
        return ComponentType.BLOCK_POS_WITH_FACE;
    }

    public static final Codec<BlockPosWithFace> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(BlockPosWithFace::getPos),
            Direction.CODEC.fieldOf("face").forGetter(BlockPosWithFace::getFace)
    ).apply(inst, BlockPosWithFace::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of();
    }
}
