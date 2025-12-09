package io.github.daxigua2333.mocai_clues.guis.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.*;
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

    /**
     * @param minecraft    Minecraft instance
     * @param x            left X of the widget
     * @param y            top Y of the widget
     * @param width        visible width
     * @param height       visible height
     * @param itemHeight   height of each row
     * @param dataSupplier supplies the current backing list (e.g. () -> myList)
     * @param labelMapper  maps T -> Component for display
     * @param clickHandler called when a row is clicked
     */
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

        // Position widget and inform layout system
        this.setX(x);
        this.setY(y); // TODO: ?
//        this.updateSizeAndPosition(x, y, width, height);
//        this.updateSizeAndPosition(width, height, y);
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
            lastSnapshot = current;

            Entry selectedEntry = null;
            UUID selectedId = null;
            if (this.getSelected() != null) {
                selectedId = this.idGetter.apply(this.getSelected().value);
            }

            List<Entry> entries = new ArrayList<>(current.size());
            for (T element : current) {
                var newEntry = new Entry(element);
                if (this.idGetter.apply(element) == selectedId) {
                    selectedEntry = newEntry;
                }
                entries.add(newEntry);
            }
            // This is the "hot update" bit: swap entries in-place.
            this.replaceEntries(entries);  // from AbstractSelectionList
            if (selectedEntry != null) {
                this.setSelected(selectedEntry);
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // auto update every tick
        syncIfNeeded();
        // manage z
        var pose = graphics.pose();
        pose.pushPose();
        pose.translate(0,0,z);
        // render
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        pose.popPose();
    }


    // ====== appearance ======
    @Override
    public int getRowWidth() {
//        Make rows slightly narrower than the full width so the scrollbar has space
        return this.width - 10;
    }
    @Override
    protected int getScrollbarPosition() {
        // Right edge of the list; SCROLLBAR_WIDTH is handled internally.
        return this.getX() + this.getWidth() - 6;
    }
    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
//        super.renderListBackground(guiGraphics);
    }

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//        super.renderDecorations(guiGraphics, mouseX, mouseY);
    }


    /** One row in the list. */
    public class Entry extends ObjectSelectionList.Entry<Entry> {

        private final T value;
        private final Component label;

        public Entry(T value) {
            this.value = value;
            this.label = labelMapper.apply(value);
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
                int bg = selected ? 0x80FFFFFF : 0x40FFFFFF;
                gfx.fill(left, top, left + width, top + height, bg);
            }

            int x = left + 4;
            int y = top + 4;

            // Title (single line)
            gfx.drawString(font, label, x, y, 0x000000, false);

//            // Optional icon
//            if (!this.value.icon().isEmpty()) {
//                gfx.renderItem(this.value.icon(), x, y);
//                x += 20;
//            }
//
//            // Subtitle in smaller gray text (clamped to row width)
//            String subtitleText = this.data.subtitle().getString();
//            if (!subtitleText.isEmpty()) {
//                int maxWidth = width - (x - left) - 4;
//                String trimmed = font.plainSubstrByWidth(subtitleText, maxWidth);
//                gfx.drawString(font, trimmed,
//                        x,
//                        y + font.lineHeight + 1,
//                        0xFFA0A0A0,
//                        false
//                );
//            }

        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) { // left click
                // play sound
                Minecraft mc = Minecraft.getInstance();
                mc.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
                );
                // Visually select the row
                AutoUpdatedScrollableListWidget.this.setSelected(this);
                // Fire your callback
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
