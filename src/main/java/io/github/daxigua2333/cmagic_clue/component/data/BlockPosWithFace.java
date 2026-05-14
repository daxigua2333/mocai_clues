package io.github.daxigua2333.cmagic_clue.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.cmagic_clue.component.ClueComponent;
import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BlockPosWithFace extends ClueComponent {
    private @Nullable BlockPos pos;
    private @Nullable Direction face;

    public BlockPosWithFace(@Nullable BlockPos pos, @Nullable Direction face) {
        this.pos = pos;
        this.face = face;
    }

    public BlockPosWithFace(@Nullable BlockPos pos) {
        this(pos, null);
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

    public void update(BlockPos pos, Direction face) {
        this.pos = pos;
        this.face = face;
    }

    public void update(BlockPos pos) {
        update(pos, null);
    }

    public void update(BlockPosWithFace other) {
        update(other.getPos(), other.getFace());
    }

    @Override
    public ComponentType type() {
        return ComponentType.BLOCK_POS_WITH_FACE;
    }

    public static final Codec<BlockPosWithFace> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BlockPos.CODEC.optionalFieldOf("pos").forGetter(obj -> Optional.ofNullable(obj.pos)),
            Direction.CODEC.optionalFieldOf("face").forGetter(obj -> Optional.ofNullable(obj.face))
    ).apply(inst, (opt1, opt2) -> new BlockPosWithFace(opt1.orElse(null), opt2.orElse(null))));

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
