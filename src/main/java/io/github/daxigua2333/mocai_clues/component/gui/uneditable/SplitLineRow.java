package io.github.daxigua2333.mocai_clues.component.gui.uneditable;

import io.github.daxigua2333.mocai_clues.component.gui.BaseDetailRow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class SplitLineRow extends BaseDetailRow {

    public SplitLineRow(int x, int y, int width, int height, int vPadding, int hPadding) {
        super(x, y, width, height, vPadding, hPadding);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        // Draw split line
        guiGraphics.hLine(
                getX() + hPadding, getX() + width - hPadding,
                getY() + vPadding, 0xD0000000);

        // adjust height
        this.height = 2 * vPadding;
    }

    // no click behavior
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}


}
