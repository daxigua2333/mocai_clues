package io.github.daxigua2333.cmagic_clue.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ClueComponent;
import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import io.github.daxigua2333.cmagic_clue.guis.widget.editable.EditBoxWithBacking;
import io.github.daxigua2333.cmagic_clue.guis.widget.uneditable.ScaledTextRow;
import io.github.daxigua2333.cmagic_clue.guis.widget.uneditable.SplitLineRow;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InfoData extends ClueComponent {
    private String name;

    public InfoData(String name) {
        this.name = name;
    }

    public InfoData() {
        this("default name");
    }

    /**
     * copy constructor
     */
    public InfoData(InfoData old) {
        this(old.getName());
    }

    public String getName() {
        return name;
    }


    @Override
    public ComponentType type() {
        return ComponentType.INFO_DATA;
    }

    public static final Codec<InfoData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(InfoData::getName)
    ).apply(instance, InfoData::new));


    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        return List.of(
                new ScaledTextRow(0, 0, 100, 100, Component.translatable(CMagicClue.MODID + ".screen.name"), 1.1f),
                EditBoxWithBacking.stringBox(0, 0, 100, 20,
//                        EditBoxRow.MutableValue.of(this.name),
                        () -> this.name, (str) -> this.name = str,
                        v -> true,
                        Component.literal("String"))
        );
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {  // TODO: uuid in editor
        return List.of(
                new ScaledTextRow(0, 0, 100, 100, Component.literal(name), 1.2f),
                new SplitLineRow(0, 0, 100, 6)
        );
    }
}
