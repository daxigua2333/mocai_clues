package io.github.daxigua2333.mocai_clues.component.gui.uneditable;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.daxigua2333.mocai_clues.component.gui.BaseDetailRow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class ScaledTextRow extends BaseDetailRow {
    private final String text;
    private final float scale;

    public ScaledTextRow(int x, int y, int width, int height, int vPadding, int hPadding, String text, float scale) {
        super(x, y, width, height, vPadding, hPadding);
        this.text = text;
        this.scale = scale;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        Component component = Component.literal(text);
        // desired position/size
        int x = getX() + hPadding;
        int y = getY() + vPadding;
        int w = this.width - 2* hPadding;
        int newWidth = (int) (w / scale);
        int newHeight = font.wordWrapHeight(component, newWidth);
        int h = (int) (newHeight * scale) + 2*vPadding;

//        // TEST: box background
//        guiGraphics.fill(
//            x - 2,
//            y - 2,
//            x + w + 2,
//            y + h + 2,
//            0xAA000000
//        );

        // scale the font size
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.scale(scale, scale, 1.0F);  // x y z

        int newX = (int) (x/scale);
        int newY = (int) (y/scale);
        for (FormattedCharSequence line : font.split(component, newWidth)) {
            guiGraphics.drawString(font, line, newX, newY, 0xFFFFFF);
            newY += font.lineHeight;
        }

        pose.popPose();

        // adjust height
        this.height = h;
    }

    // no click behavior
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        // Basic narration: title + joined content
        this.defaultButtonNarrationText(output);
        output.add(
                net.minecraft.client.gui.narration.NarratedElementType.HINT,
                Component.literal(text)
        );
    }


}
