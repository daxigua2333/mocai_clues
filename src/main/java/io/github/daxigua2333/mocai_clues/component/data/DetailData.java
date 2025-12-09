package io.github.daxigua2333.mocai_clues.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DetailData extends ClueComponent {
    private final String name;
    private final List<String> details;  // TODO: info merge

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
    public LinkedHashMap<String, AbstractWidget> getEditable() {
        return null;   // TODO
    }
    @Override
    public LinkedHashMap<String, AbstractWidget> getUneditable() {
        return null;   // TODO
    }


    // ==== getter ====
    private String getName() {
        return name;
    }
    private List<String> getDetails() {
        return details;
    }
    public void add(String item) {
        this.details.add(item);
    }
}
