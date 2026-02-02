package io.github.daxigua2333.mocai_clues.guis.widget.container;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Add features to AbstractContainerWidget:
 * - dirty reLayout (but change the former abstract #renderWidget to abstract #renderTick)
 * - clear focus when self is unfocused
 */
public abstract class FlexibleContainer extends AbstractContainerWidget {
    protected boolean dirty = false;

    protected void markDirty() {
        this.dirty = true;
    }

    private void processDirtyWithReLayout() {
        reLayout();
        processDirty();
    }

    protected abstract void processDirty();

    /**
     * reset x y w h of children, with this.x y w h
     */
    protected abstract void reLayout();


    public FlexibleContainer(int x, int y, int width, int height, Component component) {
        super(x, y, width, height, component);
        markDirty();
    }


    // dirty reLayout
    @Override
    public void setX(int x) {
        super.setX(x);
        markDirty();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        markDirty();
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        markDirty();
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        markDirty();
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        markDirty();
    }


    // render tick
    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (dirty) {
            processDirtyWithReLayout();
            dirty = false;
        }
        renderTick(graphics, mouseX, mouseY, partialTick);
    }

    protected abstract void renderTick(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);


    // focus
    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        if (getFocused() == listener) {  // compares reference
            return;
        }
        super.setFocused(listener);

    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {  // container lose focus
            this.setFocused(null);
            this.setDragging(false);
        }
    }

}
