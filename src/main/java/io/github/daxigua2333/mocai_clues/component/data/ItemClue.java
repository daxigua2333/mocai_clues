package io.github.daxigua2333.mocai_clues.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.guis.widget.editable.ItemStackPickerWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.uneditable.ItemStackSlotWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemClue extends ClueComponent {
    private ItemStack stack;

    public ItemClue(ItemStack stack) {
        this.stack = stack;
    }

    public ItemClue() {
        this(ItemStack.EMPTY);
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ComponentType type() {
        return ComponentType.ITEM_CLUE;
    }

    public static final Codec<ItemClue> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ItemStack.OPTIONAL_CODEC.fieldOf("stacks").forGetter(ItemClue::getStack)
    ).apply(inst, ItemClue::new));

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
