package io.github.daxigua2333.mocai_clues.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.guis.widget.uneditable.ScaledTextRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
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
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        Checkbox checkbox = Checkbox.builder(Component.translatable(MoCaiClues.MODID + ".screen.is_item_clue"), Minecraft.getInstance().font)
                .selected(isItemClue)
                .onValueChange((box, checked) -> {
                    markDirty.run();
                    next();
                })
                .build();
        return List.of(checkbox);
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of(
                new ScaledTextRow(0, 0, 100, 100,
                        isItemClue ? Component.translatable(MoCaiClues.MODID + ".screen.item_clue_type") : Component.translatable(MoCaiClues.MODID + ".screen.non_item_clue_type"),
                        1f)
        );
    }
}
