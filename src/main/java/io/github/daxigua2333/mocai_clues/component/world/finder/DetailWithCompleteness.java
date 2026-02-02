package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.guis.widget.uneditable.TextListWithIndexRow;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DetailWithCompleteness extends ClueComponent {
    public record Row(String text, float completeness) {
        public static final Codec<Row> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.STRING.fieldOf("text").forGetter(Row::text),
                Codec.FLOAT.optionalFieldOf("completeness").forGetter(d -> Optional.of(d.completeness))
        ).apply(inst, (opt1, opt2) -> new Row(opt1, opt2.orElse(1f))));
    }

    private final List<Row> details;

    private DetailWithCompleteness(List<Row> details) {
        this.details = details;
    }

    public DetailWithCompleteness() {
        this(new ArrayList<>());
    }


    private List<Row> getDetails() {
        return details;
    }

    public void add(String text, float completeness) {
        details.add(new Row(text, completeness));
    }


    @Override
    public ComponentType type() {
        return ComponentType.DETAIL_WITH_COMPLETENESS;
    }

    public static final Codec<DetailWithCompleteness> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Row.CODEC.listOf().fieldOf("").forGetter(DetailWithCompleteness::getDetails)
    ).apply(inst, DetailWithCompleteness::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        List<String> text = new ArrayList<>();
        for (var row : details) {
            text.add(row.text);
        }
        return List.of(
                new TextListWithIndexRow(0, 0, 100, 100, text, 2)
        );
    }
}
