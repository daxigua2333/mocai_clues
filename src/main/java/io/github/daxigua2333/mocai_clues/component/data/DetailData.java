package io.github.daxigua2333.mocai_clues.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.gui.editable.EditBoxRow;
import io.github.daxigua2333.mocai_clues.component.gui.editable.StringListWidget;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.ScaledTextRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.SplitLineRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.TextListWithIndexRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailData extends ClueComponent {
    private String name;
    private List<String> details;  // TODO: info merge

    @Override
    public ComponentType type() {
        return ComponentType.DETAIL_DATA;
    }

    // ===== constructors ====
    public DetailData(String name, List<String> details) {
        this.name = name;
        this.details = details;
    }
    public DetailData() {
        this("default name", new ArrayList<>());
    }

    // ==== codec ====
    public static final Codec<DetailData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(DetailData::getName),
            Codec.STRING.listOf().fieldOf("details").forGetter(DetailData::getDetails)
    ).apply(instance, DetailData::new));

    // ======= editable ===========
    @Override
    public List<AbstractWidget> getEditable() {
        var stringList = new StringListWidget(Minecraft.getInstance().font, 0, 0, 100, 100, this.details);
        stringList.setChangeListener(list -> this.details = list);

        return List.of(   // TODO
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.translatable("name:"), 1.1f),
                EditBoxRow.stringBox(0, 0, 100, 20, 2, 2,
//                        EditBoxRow.MutableValue.of(this.name),
                        () -> this.name, (str) -> this.name = str,
                        v -> true,
                        Component.literal("String")),
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.translatable("details:"), 1.1f),
                stringList
        );
    }
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of(
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.literal(name), 1.2f),
                new SplitLineRow(0, 0, 100, 100, 2, 2),
                new TextListWithIndexRow(0, 0, 100, 100, 2, 2, details, 2)
        );
    }


    // ==== getter ====
    public String getName() {
        return name;
    }
    public List<String> getDetails() {
        return Collections.unmodifiableList(details);
    }
    public void add(String item) {
        this.details.add(item);
    }
}
