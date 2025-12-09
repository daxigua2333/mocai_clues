package io.github.daxigua2333.mocai_clues.component.gui.uneditable;

import io.github.daxigua2333.mocai_clues.component.gui.BaseDetailRow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class TextListWithIndexRow extends BaseDetailRow {
    private final List<String> texts;
    private final int spacing;

    public TextListWithIndexRow(int x, int y, int width, int height, int vPadding, int hPadding, List<String> texts, int spacing) {
        super(x, y, width, height, vPadding, hPadding);
        this.texts = texts;
        this.spacing = spacing;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        int y = getY() + vPadding;
        for (int i=0; i<texts.size(); i++) {
            String text = texts.get(i);
            Component component = Component.literal(i+1 + ". " + text);
            guiGraphics.drawWordWrap(
                    font,
                    component,
                    getX() + hPadding,
                    y,
                    this.width - 2*hPadding,
                    0x000000
            );

            y += font.wordWrapHeight(component, width-2*hPadding);
        }

        // adjust height
        this.height = y + vPadding - getY();
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
