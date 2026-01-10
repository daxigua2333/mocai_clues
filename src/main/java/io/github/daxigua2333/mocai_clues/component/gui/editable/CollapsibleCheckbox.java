package io.github.daxigua2333.mocai_clues.component.gui.editable;

import io.github.daxigua2333.mocai_clues.component.gui.FlexibleContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CollapsibleCheckbox extends FlexibleContainer {

    private final Checkbox checkbox;
    private final List<AbstractWidget> children;

    private final Consumer<Boolean> onToggle;

    public CollapsibleCheckbox(int x, int y, int width,
                               Component title,
                               boolean initialChecked,
                               Consumer<Boolean> onToggle,
                               List<AbstractWidget> children) {
        super(x, y, width, 0, Component.empty());

        this.onToggle = onToggle;
        this.children = children;

        this.checkbox = Checkbox.builder(title, Minecraft.getInstance().font)
                .pos(x, y).selected(initialChecked)
                .onValueChange((box, checked) -> {
                    setExpanded(checked);
                    this.onToggle.accept(checked);
                })
                .build();

        reLayout();
        setExpanded(initialChecked);
    }

    private void setExpanded(boolean checked) {
        for (var w : children) {
            w.visible = checked;
            w.active = checked;
            if (!checked) w.setFocused(checked);
        }
        if (!checked && getFocused() instanceof EditBoxRow<?>) {
            setFocused(checkbox);
        }
    }
    public boolean isExpanded() {
        return checkbox.selected();
    }
    public void setChecked(boolean checked) {

    }


    @Override
    protected void processDirty() {

    }

    // layout part
    @Override
    protected void reLayout(){
        checkbox.setPosition(getX(), getY());
        checkbox.setWidth(getWidth());
        int x = getX();
        int y = getY() + checkbox.getHeight();
        for (var w : children) {
            w.setPosition(x, y);
            w.setWidth(getWidth());
            y += w.getHeight();
        }
    }

    @Override
    public int getHeight() {
        int h = checkbox.getHeight();
        for (var w : children) {
            h += w.getHeight();
        }
        return h;
    }

    @Override
    protected void renderTick(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        checkbox.render(graphics, mouseX, mouseY, partialTick);
        for (var w : children) {
            if (w.visible) w.render(graphics, mouseX, mouseY, partialTick);
        }
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> result = new ArrayList(children.size()+1);
        result.add(checkbox);
        if (isExpanded()) result.addAll(children);
        return result;
    }
}
