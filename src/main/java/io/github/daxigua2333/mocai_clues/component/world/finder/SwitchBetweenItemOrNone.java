package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SwitchBetweenItemOrNone extends ClueComponent {
    private boolean isItemClue;

    public SwitchBetweenItemOrNone() {
        this(false);
    }

    public SwitchBetweenItemOrNone(boolean isItemClue) {
        this.isItemClue = isItemClue;
    }

    public boolean isItemClue() {
        return isItemClue;
    }

    public void next() {
        isItemClue = !isItemClue;
    }

    @Override
    public ComponentType type() {
        return ComponentType.SWITCH_BETWEEN_ITEM_OR_NONE;
    }

    public static final Codec<SwitchBetweenItemOrNone> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("isItemClue").forGetter(SwitchBetweenItemOrNone::isItemClue)
    ).apply(inst, SwitchBetweenItemOrNone::new));

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
