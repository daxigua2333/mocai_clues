package io.github.daxigua2333.mocai_clues.component.gui.uneditable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ReadOnlyDetailWidget extends AbstractWidget {

    private final String title;
    private final List<String> content;
    private final Font font;

    // Padding / layout constants
    private static final int PADDING = 4;
    private static final int TITLE_SPACING = 4;
    private static final int LINE_SPACING = 2;

    public ReadOnlyDetailWidget(int x, int y, int width, int height,
                                String title, List<String> content) {
        super(x, y, width, height, Component.literal(title));
        this.title = title;
        this.content = content;
        this.font = Minecraft.getInstance().font;

    }


    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        // Background box
        // Slightly transparent dark background
//        RenderSystem.enableBlend();
//        guiGraphics.fill(
//                getX(), getY(),
//                getX() + width, getY() + height,
//                0xAA000000      // ARGB
//        );
//        // Border
//        guiGraphics.renderOutline(
//                getX(), getY(),
//                width, height,
//                0xFFFFFFFF
//        );

        int x = getX() + PADDING;
        int y = getY() + PADDING;

        // Draw title (in white, bold-ish effect by shadow)
        Component component = Component.literal(title);
        guiGraphics.drawWordWrap(
                font,
                component,
                x,
                y,
                this.width,
                0x000000
        );

        // Move down after title
        y += font.wordWrapHeight(component, this.width) + TITLE_SPACING;

        // Draw split line
        guiGraphics.hLine(x, x+50, y, 0xD0000000);  // TODO: width
        y += TITLE_SPACING;

        // Draw content lines
        for (int i=0; i<content.size(); i++) {
//            if (y + font.lineHeight > getY() + height - PADDING) {
//                // No more vertical space – stop drawing
//                break;
//            }
            String line = content.get(i);
            component = Component.literal(i+1 + ". " + line);
            guiGraphics.drawWordWrap(
                    font,
                    component,
                    x,
                    y,
                    this.width,
                    0x000000
            );

            y += font.wordWrapHeight(component, this.width) + LINE_SPACING;  // height
        }

        // update height to make it contains all the texts
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
        String joined = String.join(", ", content);
        this.defaultButtonNarrationText(output);
        output.add(
                net.minecraft.client.gui.narration.NarratedElementType.HINT,
                Component.literal(joined)
        );
    }


}
