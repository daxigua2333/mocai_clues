package io.github.daxigua2333.mocai_clues.guis.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DropdownWidget<T> extends AbstractWidget {
    private List<T> options;
    private final Supplier<List<T>> optionsSupplier;
    private final Function<T, Component> labelFunc;
    private final Consumer<T> onChange;

    private int selectedIndex;
    private boolean open = false;

    private final int baseHeight;
    private final int itemHeight = 20;

    private final int z;

    public DropdownWidget(
            int x, int y, int z, int width, int height,
//            List<T> options,
            int initialIndex,
            Supplier<List<T>> optionsSupplier,
            Function<T, Component> labelFunc,
            Consumer<T> onChange
    ) {
        super(x, y, width, height, Component.empty());
//        List<T> options = optionsSupplier.get(); ///////
//        if (options.isEmpty()) {
//            throw new IllegalArgumentException("DropdownWidget options list cannot be empty");
//        }
        this.optionsSupplier = optionsSupplier;
        this.options = optionsSupplier.get();
        this.labelFunc = labelFunc;
        this.onChange = onChange;
        this.selectedIndex = Math.max(0, Math.min(initialIndex, options.size() - 1));
        this.baseHeight = height;
//        this.setMessage(labelFunc.apply(options.get(this.selectedIndex)));
        this.z = z;

        if (!options.isEmpty()) {
            onChange.accept(options.get(selectedIndex));
        }
    }

    public T getSelected() {
        return options.get(selectedIndex);
    }

//    public void setSelectedIndex(int index) {
//        if (index >= 0 && index < options.size()) {
//            this.selectedIndex = index;
//            this.setMessage(labelFunc.apply(options.get(selectedIndex)));
//        }
//    }

    // ========= sync part =======
    private void syncIfNeeded() {
        options = List.copyOf(optionsSupplier.get());
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        syncIfNeeded();

        // manage z
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0,0,z);

        // Draw "main" box
        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.baseHeight;

        // background color (simple)
        int bgColor = this.isHoveredOrFocused() ? 0xFF666666 : 0xFF444444;
        graphics.fill(x, y, x + w, y + h, bgColor);

        // border
        graphics.fill(x, y, x + w, y + 1, 0xFF000000);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF000000);
        graphics.fill(x, y, x + 1, y + h, 0xFF000000);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFF000000);

        // current label
        var font = Minecraft.getInstance().font;
        Component currentLabel = options.isEmpty() ? Component.literal("") : labelFunc.apply(options.get(selectedIndex));
        int textX = x + 4;
        int textY = y + (h - font.lineHeight) / 2;
        graphics.drawString(font, currentLabel, textX, textY, 0xFFFFFFFF, false);

        // little ▼ arrow on the right
        graphics.drawString(
                font,
                Component.literal(open ? "▲" : "▼"),
                x + w - 10,
                textY,
                0xFFFFFFFF,
                false
        );

        // If open, draw the list below
        if (open) {
            int listTop = y + h;
            for (int i = 0; i < options.size(); i++) {
                int itemY = listTop + i * itemHeight;
                int itemBottom = itemY + itemHeight;

                boolean hovered = mouseX >= x && mouseX < x + w &&
                                  mouseY >= itemY && mouseY < itemBottom;

                int itemBg = hovered ? 0xFF777777 : 0xFF555555;
                graphics.fill(x, itemY, x + w, itemBottom, itemBg);

                Component label = labelFunc.apply(options.get(i));
                int itemTextY = itemY + (itemHeight - font.lineHeight) / 2;
                graphics.drawString(font, label, x + 4, itemTextY, 0xFFFFFFFF, false);

                // simple separator line
                graphics.fill(x, itemBottom - 1, x + w, itemBottom, 0xFF000000);

            }
        }
        pose.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || !this.isValidClickButton(button)) {
            return false;
        }

        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.baseHeight;

        // Click on the main box -> toggle open/close
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            this.open = !this.open;
            this.updateHeightForOpenState();
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        // If open, handle clicks in the list
        if (open) {
            int listTop = y + h;
            int listBottom = listTop + options.size() * itemHeight;

            if (mouseX >= x && mouseX < x + w && mouseY >= listTop && mouseY < listBottom) {
                int index = (int) ((mouseY - listTop) / itemHeight);
                if (index >= 0 && index < options.size()) {
                    this.selectedIndex = index;
                    this.setMessage(labelFunc.apply(options.get(selectedIndex)));
                    if (onChange != null) {
                        onChange.accept(options.get(selectedIndex));
                    }
                }
                this.open = false;
                this.updateHeightForOpenState();
                return true;
            } else {
                // Click outside closes it
                this.open = false;
                this.updateHeightForOpenState();
                // Return false so other widgets can handle the click
            }
        }

        return false;
    }

    private void updateHeightForOpenState() {
        if (open) {
            this.setHeight(baseHeight + options.size() * itemHeight);
        } else {
            this.setHeight(baseHeight);
        }
    }

    @Override
    protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput narration) {
        // For brevity: you can add narration text here if you want full accessibility support.
    }
}
