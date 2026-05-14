package io.github.daxigua2333.cmagic_clue.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ClueComponent;
import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import io.github.daxigua2333.cmagic_clue.guis.widget.editable.StringListWidget;
import io.github.daxigua2333.cmagic_clue.guis.widget.uneditable.ScaledTextRow;
import io.github.daxigua2333.cmagic_clue.guis.widget.uneditable.TextListWithIndexRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailData extends ClueComponent {
    private List<String> details;

    @Override
    public ComponentType type() {
        return ComponentType.DETAIL_DATA;
    }

    // ===== constructors ====
    public DetailData(List<String> details) {
        this.details = details;
    }

    public DetailData() {
        this(new ArrayList<>());
    }

    // ==== codec ====
    public static final Codec<DetailData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("details").forGetter(DetailData::getDetails)
    ).apply(instance, DetailData::new));

    // ======= editable ===========
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
//        SwitchBetweenItemOrNone sCompo = owner.getComponent(ComponentType.SWITCH_BETWEEN_ITEM_OR_NONE);
//        if (sCompo != null && sCompo.isItemClue()) {
//            return List.of();
//        }

        var stringList = new StringListWidget(Minecraft.getInstance().font, 0, 0, 100, 100, this.details);
        stringList.setChangeListener(list -> this.details = list);

        return List.of(
                new ScaledTextRow(0, 0, 100, 100, Component.translatable(CMagicClue.MODID + ".screen.details"), 1.1f),
                stringList
        );
    }

    @Override
    public List<AbstractWidget> getUneditable() {
//        SwitchBetweenItemOrNone sCompo = owner.getComponent(ComponentType.SWITCH_BETWEEN_ITEM_OR_NONE);
//        if (sCompo != null && sCompo.isItemClue()) {
//            return List.of();
//        }

        return List.of(
                new TextListWithIndexRow(0, 0, 100, 100, details, 2)
        );
    }


    // ==== getter ====
    public List<String> getDetails() {
        return Collections.unmodifiableList(details);
    }

    public void add(String item) {
        this.details.add(item);
    }
}
