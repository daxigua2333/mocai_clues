package io.github.daxigua2333.mocai_clues.guis.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AutoUpdatedScrollableListWidget<T> extends ObjectSelectionList<AutoUpdatedScrollableListWidget<T>.Entry> {

    private Supplier<List<T>> dataSupplier;
    private final Function<T, UUID> idGetter;
    private final Function<T, Component> labelMapper;
    private final Consumer<T> clickHandler;
    private final int z;

    // Snapshot of the last data we rendered
    private List<T> lastSnapshot = List.of();

    public AutoUpdatedScrollableListWidget(Minecraft minecraft,
                                           int x, int y, int z,
                                           int width, int height,
                                           int itemHeight,
                                           Supplier<List<T>> dataSupplier,
                                           Function<T, UUID> idGetter,
                                           Function<T, Component> labelMapper,
                                           Consumer<T> clickHandler) {
        super(minecraft, width, height, y, itemHeight);

        this.dataSupplier = dataSupplier;
        this.idGetter = idGetter;
        this.labelMapper = labelMapper;
        this.clickHandler = clickHandler;
        this.z = z;

        this.setX(x);
        this.setY(y);
    }

    public void updateDataSupplier(Supplier<List<T>> dataSupplier) {
        this.dataSupplier = dataSupplier;
    }


    // ========= sync part =======
    // each tick check once. if changed, **rebuild** the Entries
    // TODO: performance..
    private void syncIfNeeded() {
        List<T> current = List.copyOf(dataSupplier.get());
        if (!current.equals(lastSnapshot)) {
            Entry selectedEntry = null;
            UUID selectedId = null;
            if (this.getSelected() != null) {
                selectedId = this.idGetter.apply(this.getSelected().value);
            }

            lastSnapshot = current;

            List<Entry> entries = new ArrayList<>(current.size());
            for (T element : current) {
                var newEntry = new Entry(element);
                if (selectedId != null && this.idGetter.apply(element).equals(selectedId)) {
                    selectedEntry = newEntry;
                }
                entries.add(newEntry);
            }
            this.replaceEntries(entries);

            // Restore selection if the item still exists
            if (selectedEntry != null) {
                this.setSelected(selectedEntry);
                clickHandler.accept(selectedEntry.value);
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        syncIfNeeded();

        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, z);

        // This calls ObjectSelectionList.renderWidget, which enables Scissor
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        pose.popPose();
    }


    // ====== appearance ======
    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.getWidth();
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
        // Transparent background
    }

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
    }


    /**
     * One row in the list.
     */
    public class Entry extends ObjectSelectionList.Entry<Entry> {

        private final T value;
        private final Component label;

        public Entry(T value) {
            this.value = value;
            this.label = labelMapper.apply(value);
        }

        public T getValue() {
            return value;
        }

        @Override
        public void render(GuiGraphics gfx,
                           int index,
                           int top,
                           int left,
                           int width,
                           int height,
                           int mouseX,
                           int mouseY,
                           boolean hovered,
                           float partialTick) {

            Font font = Minecraft.getInstance().font;

            boolean selected = AutoUpdatedScrollableListWidget.this.getSelected() == this;

            // Highlight selected/hovered rows
            if (hovered || selected) {
                int bg = selected ? 0x40000000 : 0x30000000;
                gfx.fill(left, top, left + width, top + height, bg);
            }

            int x = left + 2;
            int y = top + 4;

            // Title (single line)
            FormattedText truncated = font.getSplitter().headByWidth(label, width, Style.EMPTY);
            FormattedCharSequence visualText = Language.getInstance().getVisualOrder(truncated);
            gfx.drawString(font, visualText, x, y, 0x000000, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) { // left click
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
                );
                AutoUpdatedScrollableListWidget.this.setSelected(this);
                clickHandler.accept(value);
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return label;
        }
    }
}
