package io.github.daxigua2333.cmagic_clue.guis.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class FixedTextureButton extends Button {
    private final ResourceLocation texture;
//    private final int texW;
//    private final int texH;

    public FixedTextureButton(int x, int y, int width, int height, Component message, OnPress onPress, ResourceLocation texture) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
    }

    @Override
    protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // draw the same texture regardless of hovered/focused/disabled
        g.setColor(1f, 1f, 1f, this.alpha);
//        g.blit(texture, this.getX(), this.getY(), 0, 0, this.width, this.height, width, height);
        g.blitSprite(texture, getX(), getY(), width, height);
        g.setColor(1f, 1f, 1f, 1f);
//        // optional text (constant color, no hover/focus color changes)
//        if (!this.getMessage().getString().isEmpty()) {
//            int argb = ((int)(this.alpha * 255) << 24) | 0xFFFFFF;
//            g.drawCenteredString(
//                    Minecraft.getInstance().font,
//                    this.getMessage(),
//                    this.getX() + this.width / 2,
//                    this.getY() + (this.height - 8) / 2,
//                    argb
//            );
//        }
    }
}
