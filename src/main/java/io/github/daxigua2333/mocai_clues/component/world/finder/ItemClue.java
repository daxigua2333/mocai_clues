package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemClue extends ClueComponent {
    private BlockPos pos;
    private Direction face;
    private ItemStack stack;

    private ItemClue(ItemStack stack, Direction face, BlockPos pos) {
        this.stack = stack;
        this.face = face;
        this.pos = pos;
    }

    public ItemClue() {
        this(ItemStack.EMPTY, null, null);
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getFace() {
        return face;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }

    public void setFace(Direction face) {
        this.face = face;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ComponentType type() {
        return ComponentType.ITEM_CLUE;
    }

    public static final Codec<ItemClue> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("stacks").forGetter(ItemClue::getStack),
            Direction.CODEC.fieldOf("face").forGetter(ItemClue::getFace),
            BlockPos.CODEC.fieldOf("pos").forGetter(ItemClue::getPos)
    ).apply(inst, ItemClue::new));

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
