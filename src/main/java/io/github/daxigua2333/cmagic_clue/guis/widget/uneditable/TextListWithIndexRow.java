package io.github.daxigua2333.cmagic_clue.guis.widget.uneditable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TextListWithIndexRow extends AbstractWidget {
    private final List<String> texts;
    private final int spacing;
    private final Font font;

    public TextListWithIndexRow(int x, int y, int width, int height, List<String> texts, int spacing) {
        super(x, y, width, height, Component.empty());
        this.texts = texts;
        this.spacing = spacing;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        int y = getY();
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            Component component = Component.literal(i + 1 + ". " + text);  // TODO: lazy with Component
            guiGraphics.drawWordWrap(
                    font,
                    component,
                    getX(),
                    y,
                    this.width,
                    0x000000
            );

            y += font.wordWrapHeight(component, width) + spacing;
        }

        // adjust height
        this.height = y - getY();
    }

    // no click behavior
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        // Basic narration: title + joined content
        String joined = String.join(", ", texts);
        this.defaultButtonNarrationText(output);
        output.add(
                net.minecraft.client.gui.narration.NarratedElementType.HINT,
                Component.literal(joined)
        );
    }


}
