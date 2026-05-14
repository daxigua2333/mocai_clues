package io.github.daxigua2333.cmagic_clue.guis.widget.uneditable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ScaledTextRow extends AbstractWidget {
    private final Component text;
    private final float scale;

    public ScaledTextRow(int x, int y, int width, int height, Component text, float scale) {
        super(x, y, width, height, Component.empty());
        this.text = text;
        this.scale = scale;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        // desired position/size
        Font font = Minecraft.getInstance().font;
        int x = getX();
        int y = getY();
        int w = this.width;
        int newWidth = (int) (w / scale);
        int newHeight = font.wordWrapHeight(text, newWidth);
        int h = (int) (newHeight * scale);
        // adjust height
        this.height = h;

        // scale the font size
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1.0F);

        guiGraphics.drawWordWrap(font, text, 0, 0, newWidth, 0x000000);

        pose.popPose();
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
                text
        );
    }


}
