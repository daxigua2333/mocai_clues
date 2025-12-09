package io.github.daxigua2333.mocai_clues.guis.widget;

import com.mojang.blaze3d.vertex.Tesselator;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * A scrollable container for various widgets.
 */
public class ScrollableConfigPanel extends ScrollPanel {

    private final List<AbstractWidget> children = new ArrayList<>();

    private final EditBox textBox;
    private final VolumeSlider slider;
    private final List<Button> optionButtons = new ArrayList<>();

    // layout info (content-space, NOT screen-space)
    private final int contentWidth;
    private float lastPartialTick;

    // Which child currently has keyboard focus
    private AbstractWidget focusedChild;

    public ScrollableConfigPanel(Minecraft mc, int width, int height, int top, int left) {
        // border = 6 just for looks; tweak as you like
        super(mc, width, height, top, left, 6);

        Font font = mc.font;
        this.contentWidth = width - this.border * 2;

        int y = 0;

        // 1) Text field
        this.textBox = new EditBox(
                font,
                0, 0, // x / y will be set in drawPanel
                this.contentWidth,
                20,
                Component.literal("Text input")
        );
        this.textBox.setValue("Hello NeoForge 1.21.1");
        this.children.add(this.textBox);
        y += 20 + 6;

        // 2) Slider
        this.slider = new VolumeSlider(0, 0, this.contentWidth, 20, 0.5D);
        this.children.add(this.slider);
        y += 20 + 6;

        // 3) Option buttons (simple “selectable list”)
        for (int i = 0; i < 15; i++) {
            int idx = i;
            Button button = Button.builder(Component.literal("Option " + i), b -> {
                        // handle selection here (e.g. save selected index)
                        System.out.println("Selected option " + idx);
                    })
                    .bounds(0, 0, this.contentWidth, 20)
                    .build();
            this.optionButtons.add(button);
            this.children.add(button);
            y += 20 + 2;
        }
    }

    // record partial tick so we can use it inside drawPanel()
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.lastPartialTick = partialTick;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // how tall your entire content is (in pixels, *before* scrolling)
    @Override
    protected int getContentHeight() {
        // 20 (textbox) + 6 spacing
        // 20 (slider) + 6 spacing
        // 15 * (20 height + 2 spacing)
        return 20 + 6 + 20 + 6 + optionButtons.size() * (20 + 2) + Minecraft.getInstance().font.lineHeight + 6;
//        // Simplest: compute based on last widget’s bottom
//        AbstractWidget last = children.getLast();
//        return (last.getY() + last.getHeight()) - this.top + 4;
    }

    /**
     * Draws the content.
     * `relativeY` is effectively "top of content area minus scroll amount".
     */
    @Override
    protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess,
                             int mouseX, int mouseY) {

        Font font = Minecraft.getInstance().font;

        int x = this.left + this.border;
        int y = relativeY;

        // title text (not a widget, just drawn text)
        guiGraphics.drawString(
                font,
                Component.literal("Scrollable config panel"),
                x,
                y,
                0xFFFFFF
        );
        y += font.lineHeight + 6;

//        int scroll = (int) this.scrollDistance;
//        for (AbstractWidget w : children) {
//            int oldY = w.getY();
//            // move into the visible window by subtracting scroll
//            w.setY(oldY - scroll);
//            w.render(guiGraphics, mouseX, mouseY, 0);
//            w.setY(oldY); // restore so logic/layout still uses unscrolled coords
//        }

        // 1) TextBox
        this.textBox.setX(x);
        this.textBox.setY(y);
        this.textBox.render(guiGraphics, mouseX, mouseY, this.lastPartialTick);
        y += this.textBox.getHeight() + 6;

        // 2) Slider
        this.slider.setX(x);
        this.slider.setY(y);
        this.slider.render(guiGraphics, mouseX, mouseY, this.lastPartialTick);
        y += this.slider.getHeight() + 6;

        // 3) Option buttons
        for (Button button : this.optionButtons) {
            button.setX(x);
            button.setY(y);
            button.render(guiGraphics, mouseX, mouseY, this.lastPartialTick);
            y += button.getHeight() + 2;
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


    /**
     * Simple slider implementation (0.0 – 1.0) that updates its label as it moves.
     */
    private static class VolumeSlider extends AbstractSliderButton {

        public VolumeSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int percent = (int) Math.round(this.value * 100.0);
            this.setMessage(Component.literal("Volume: " + percent + "%"));
        }

        @Override
        protected void applyValue() {
            // TODO: save the new value to your config / send to server
            // this.value is 0.0 – 1.0
        }
    }
}
