package io.github.daxigua2333.mocai_clues.guis.widget;

import com.mojang.blaze3d.vertex.Tesselator;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;

import java.util.ArrayList;
import java.util.List;


/**
 * a scrollable container for nested widgets
 * mainly pay attention to 4 methods:
 * constructor, getContentHeight, drawPanel (postion the widgets), children (for internally input handling)
 * */
public class DetailPanel extends ScrollPanel {

    private ClueObject content;
    private List<AbstractWidget> children = new ArrayList<>();
    private final int spacing;  // spacing between each widgets

    private float lastPartialTick;

    public DetailPanel(Minecraft mc, int width, int height, int top, int left, List<AbstractWidget> children) {
        super(mc, width, height, top, left, 0, 6 , -16777216, -8355712, -4144960);  // TODO: bg...etc

        Font font = Minecraft.getInstance().font;
        this.children = children;
        this.spacing = 6;
    }

    /** hot update*/
    public void updateChildren(ClueObject content, List<AbstractWidget> children) {
        this.content = content;
        this.children = children;
    }
    public void updateChildren(List<AbstractWidget> children) {
        this.children = children;
    }

    /** the entire content height (which means can exceed the screen height) */
    @Override
    protected int getContentHeight() {
        int result = 0;
        for (var widget : this.children) {
            result += widget.getHeight() + this.spacing;
        }
        if (result < this.height) {  // disable the default behavior when ContentHeight is smaller than panel height
            result = height;
        }
        return result;
    }

    /** invoked each tick. Mainly position widgets here.
     * relativeY: y value which has counted the scrollDistance
     * */
    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess,
                             int mouseX, int mouseY) {
        int x = this.left + this.border;
        int y = relativeY;
//        MoCaiClues.LOGGER.debug("{}", relativeY);  // after each scroll it goes into 6, -14, -34

        for (var w : this.children) {
            w.setX(x);
            w.setY(y);
            w.setWidth(this.width);
            w.render(guiGraphics, mouseX, mouseY, this.lastPartialTick);
            y += w.getHeight() + this.spacing;
        }
    }

    /**
     * will internally go through this to handle input (mouse click/release/drag/scrolled)
     */
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }


    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    // record partial tick, so we can use it inside drawPanel()
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.lastPartialTick = partialTick;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // no background
    @Override
    protected void drawBackground(GuiGraphics guiGraphics, Tesselator tess, float partialTick) {
//        Screen.renderMenuBackgroundTexture(guiGraphics, Screen.MENU_BACKGROUND, this.left, this.top, 0.0F, 0.0F, this.width, this.height);
    }



}
