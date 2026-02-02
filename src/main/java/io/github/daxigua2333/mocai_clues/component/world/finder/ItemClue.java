package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.guis.widget.ItemStackPickerWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.ItemStackSlotWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ItemClue extends ClueComponent {
    private BlockPos pos;
    @Nullable
    private Direction face;
    @Nullable
    private ItemStack stack;

    private ItemClue(ItemStack stack, Direction face, BlockPos pos) {
        this.stack = stack;
        this.face = face;
        this.pos = pos;
    }

    public ItemClue() {
        this(ItemStack.EMPTY, null, null);
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }

    @Nullable
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
            Direction.CODEC.optionalFieldOf("face").forGetter(obj -> Optional.ofNullable(obj.getFace())),
            BlockPos.CODEC.optionalFieldOf("pos").forGetter(obj -> Optional.ofNullable(obj.getPos()))
    ).apply(inst, (stack, face, pos) -> new ItemClue(stack, face.orElse(null), pos.orElse(null))));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        SwitchBetweenItemOrNone sCompo = owner.getComponent(ComponentType.SWITCH_BETWEEN_ITEM_OR_NONE);
        if (sCompo != null && !sCompo.isItemClue()) {
            return List.of();
        }

        return List.of(new ItemStackPickerWidget(0, 0, Component.empty(), () -> stack));
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        SwitchBetweenItemOrNone sCompo = owner.getComponent(ComponentType.SWITCH_BETWEEN_ITEM_OR_NONE);
        if (sCompo != null && !sCompo.isItemClue()) {
            return List.of();
        }

        return List.of(new ItemStackSlotWidget(0, 0, () -> stack));
    }
}
