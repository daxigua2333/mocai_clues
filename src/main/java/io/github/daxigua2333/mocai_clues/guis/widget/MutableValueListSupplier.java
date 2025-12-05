package io.github.daxigua2333.mocai_clues.guis.widget;

import net.minecraft.client.gui.components.CycleButton;

import java.util.List;

public class MutableValueListSupplier<T> implements CycleButton.ValueListSupplier<T> {
    private List<T> values = List.of(); // start empty or with defaults

    public void setValues(List<T> newValues) {
        // safe copy so callers can't mutate your internal list accidentally
        this.values = List.copyOf(newValues);
    }

    @Override
    public List<T> getSelectedList() {
        return values;
    }

    @Override
    public List<T> getDefaultList() {
        return values;
    }
}