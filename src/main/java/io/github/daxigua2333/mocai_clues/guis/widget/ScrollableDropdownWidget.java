package io.github.daxigua2333.mocai_clues.guis.widget;


import io.github.daxigua2333.mocai_clues.guis.widget.container.FlexibleContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ScrollableDropdownWidget<T> extends FlexibleContainer {
    private boolean open = false;

    private final AutoUpdatedScrollableListWidget<T> dropdownList;
    private final MainBox box;

    private final Function<T, Component> labelFunc;
    private final Consumer<@Nullable T> onChange;
    private final int z;

    private final int boxH = 16;
    private final int itemH = 20;
    private final int itemLimit = 5;

    // TODO: height things: per item, item count limit, open/close height update
    public ScrollableDropdownWidget(
            int x, int y, int z, int width,// int boxHeight, int itemHeight, int itemLimit,
            int initialIndex,
            Supplier<List<T>> optionsSupplier,
            Function<@Nullable T, Component> labelFunc,
            Function<T, UUID> idGetter,
            Consumer<@Nullable T> onChange
    ) {
        super(x, y, width, 0, Component.empty());
        this.labelFunc = labelFunc;
        this.onChange = onChange;
        this.z = z;

        this.box = new MainBox(x, y, width, boxH, Component.empty());
        this.dropdownList = new AutoUpdatedScrollableListWidget<>(Minecraft.getInstance(),
                x, y + height, z, width, itemH * itemLimit, itemH,
                optionsSupplier, idGetter, labelFunc,
                selected -> {
                    // updateHeightForOpenState()
                    setOpen(false);
                    box.setSelected(selected);
                });

        // init box
        List<T> options = optionsSupplier.get();
        box.setSelected((options != null && initialIndex >= 0 && initialIndex < options.size()) ? options.get(initialIndex) : null);
    }

    public boolean isOpen() {
        return open;
    }

    private void setOpen(boolean open) {
        this.open = open;
        markDirty();
    }

    private void switchOpenState() {
        setOpen(!open);
    }


    public @Nullable T getSelected() {
        return box.getSelected();
    }


    /**
     * Dirty when open state changes
     */
    @Override
    protected void processDirty() {
        dropdownList.visible = open;
    }

    @Override
    protected void reLayout() {
        box.setPosition(getX(), getY());
        dropdownList.setPosition(getX() - 2, getY() + getHeight());
        this.setHeight(isOpen() ? boxH + itemLimit * itemH : boxH);
    }

    @Override
    protected void renderTick(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, z);

        box.renderWidget(graphics, mouseX, mouseY, partialTick);
        // If open, draw the list below
        if (isOpen()) {
            int x = this.getX();
            int y = dropdownList.getY();
            graphics.fill(x, y, x + dropdownList.getWidth(), y + dropdownList.getHeight(), 0xFF777777);
            dropdownList.renderWidget(graphics, mouseX, mouseY, partialTick);
        }

        pose.popPose();
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return dropdownList.visible ? List.of(box, dropdownList) : List.of(box);
    }

    private class MainBox extends AbstractWidget {
        private @Nullable T selected;

        public MainBox(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }


        private void setSelected(@Nullable T newSelected) {
            if (!Objects.equals(newSelected, selected)) {
                selected = newSelected;
                onChange.accept(selected);
            }
        }

        public @Nullable T getSelected() {
            return selected;
        }


        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            int w = this.width;
            int h = this.height;

            // --- Render Main Box ---
            int bgColor = this.isHoveredOrFocused() ? 0xFF666666 : 0xFF555555;
            graphics.fill(x, y, x + w, y + h, bgColor);
            // Borders
            graphics.fill(x, y, x + w, y + 1, 0xFF000000);
            graphics.fill(x, y + h - 1, x + w, y + h, 0xFF000000);
            graphics.fill(x, y, x + 1, y + h, 0xFF000000);
            graphics.fill(x + w - 1, y, x + w, y + h, 0xFF000000);

            // Label
            var font = Minecraft.getInstance().font;
            Component currentLabel = labelFunc.apply(getSelected());
            int textX = x + 4;
            int textY = y + (h - font.lineHeight) / 2;
            // Clip text if it's too long for the box
            String clippedLabel = font.plainSubstrByWidth(currentLabel.getString(), w - 14);
            graphics.drawString(font, clippedLabel, textX, textY, 0xFFFFFFFF, false);
            // Arrow
            graphics.drawString(
                    font,
                    Component.literal(open ? "▲" : "▼"),
                    x + w - 10,
                    textY,
                    0xFFFFFFFF,
                    false
            );

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.active || !this.visible || !this.isValidClickButton(button)) {
                return false;
            }

            int x = this.getX();
            int y = this.getY();
            int w = this.width;
            int h = this.height;

            // Toggle Open/Close
            if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
                switchOpenState();
                this.playDownSound(Minecraft.getInstance().getSoundManager());
                return true;
            }

            return false;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

    }
}
