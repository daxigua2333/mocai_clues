package io.github.daxigua2333.mocai_clues.component.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public abstract class BaseDetailRow extends AbstractWidget {
    protected final Font font;
    protected final int vPadding;
    protected final int hPadding;

    protected BaseDetailRow(int x, int y, int width, int height, int vPadding, int hPadding) {
        super(x, y, width, height, Component.literal(""));
        this.vPadding = vPadding;
        this.hPadding = hPadding;
        this.font = Minecraft.getInstance().font;
    }
}
