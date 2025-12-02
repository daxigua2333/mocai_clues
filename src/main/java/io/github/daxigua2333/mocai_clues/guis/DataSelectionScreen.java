package io.github.daxigua2333.mocai_clues.guis;

// package your.modid.client.screen;

import java.util.List;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * A "villager trades style" screen:
 * - Left: scrollable list of entries
 * - Right: details of the selected entry
 *
 * 1.21.1+ uses GuiGraphics for rendering and AbstractSelectionList/ObjectSelectionList
 * for scrollable lists.
 */
public class DataSelectionScreen extends Screen {

    /**
     * Your data model for one row in the list.
     * Add whatever fields you need here.
     */
    public record ListEntryData(
            Component title,
            Component subtitle,
            Component detail,
            ItemStack icon // can be ItemStack.EMPTY if you don't want an icon
    ) {}

    private final List<ListEntryData> entries;

    private DataListWidget list;
    private ListEntryData selected;

    private static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "textures/gui/casebook.png");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_X_OFFSET = 15;
    private static final int PANEL_Y_OFFSET = 43;
    private static final int PANEL_WIDTH = 243 - PANEL_X_OFFSET;
    private static final int PANEL_HEIGHT = 222 - PANEL_Y_OFFSET;
    private static final int LIST_X_OFFSET = 28;
    private static final int LIST_Y_OFFSET = 56;
    private static final int LIST_WIDTH = 89 - LIST_X_OFFSET;
    private static final int LIST_HEIGHT = 210 - LIST_Y_OFFSET;
    private static final int SPLIT_X_OFFSET = 97;
    private static final int ENTRY_HEIGHT = 20;


    public DataSelectionScreen(List<ListEntryData> entries) {
        super(Component.translatable("screen.yourmodid.data_selection"));
        this.entries = entries;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();

        this.list = new DataListWidget(
                this.minecraft,
                LIST_WIDTH,
                LIST_HEIGHT,
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET,
                ENTRY_HEIGHT
        );
        this.list.setX((this.width - TEXTURE_WIDTH) / 2 + LIST_X_OFFSET);

        // Fill the list with entries
        for (ListEntryData data : this.entries) {
            this.list.addEntry(new DataListEntry(this.list, this, data));
        }

        this.addRenderableWidget(this.list);
    }

    void setSelected(ListEntryData data) {
        this.selected = data;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);

        int panelRight = (this.width + PANEL_WIDTH) / 2;
        int panelTop = (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET;
        int splitX = (this.width - TEXTURE_WIDTH) / 2 + SPLIT_X_OFFSET;

        // Let vanilla render widgets (including our list)
        super.render(gfx, mouseX, mouseY, partialTick);

        // Detail view on the right
        if (this.selected != null) {
            drawDetailPanel(gfx, this.selected, splitX, panelTop, panelRight);
        } else {
            Component hint = Component.translatable("screen.yourmodid.data_selection.hint");
            gfx.drawString(this.font, hint, splitX + 8, panelTop + 8, 0xFFA0A0A0, false);
        }

        // Tooltips (e.g. from item icon)
//        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - TEXTURE_WIDTH) / 2;
        int top = (this.height - TEXTURE_HEIGHT) / 2;

        // Your book texture
        gfx.blit(BOOK_TEXTURE, left, top, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private void drawDetailPanel(GuiGraphics gfx, ListEntryData data,
                                 int splitX, int panelTop, int panelRight) {
        int textX = splitX + 8;
        int textY = panelTop + 8;

        // Title
        gfx.drawString(this.font, data.title(), textX, textY, 0xFFFFFF, false);
        textY += this.font.lineHeight + 2;

        // Subtitle (smaller / gray)
        if (!data.subtitle().getString().isEmpty()) {
            gfx.drawString(this.font, data.subtitle(), textX, textY, 0xFFA0A0A0, false);
            textY += this.font.lineHeight + 4;
        }

        // Optional icon on the left of the detail text
        if (!data.icon().isEmpty()) {
            gfx.renderItem(data.icon(), textX, textY);
            textX += 20;
        }

        // Word-wrapped detail text
        int maxDetailWidth = panelRight - textX - 8;
        gfx.drawWordWrap(this.font, data.detail(), textX, textY, maxDetailWidth, 0xFFE0E0E0);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    // ---------------------------------------------------------
    // Left column: scrollable list
    // ---------------------------------------------------------

    /**
     * Scrollable list widget. Uses AbstractSelectionList/ObjectSelectionList
     * which is the standard way to do lists in 1.21.1
     */
    private static class DataListWidget extends ObjectSelectionList<DataListEntry> {

        public DataListWidget(Minecraft mc,
                              int width,
                              int height,
                              int top,
                              int itemHeight) {
            super(mc, width, height, top, itemHeight);
        }

        @Override
        public int getRowWidth() {
            // Use the full width for each row
            return this.getWidth();
        }

        @Override
        protected int getScrollbarPosition() {
            // Right edge of the list; SCROLLBAR_WIDTH is handled internally.
            return this.getX() + this.getWidth() - 6;
        }

        @Override
        public int addEntry(DataListEntry entry) {
            return super.addEntry(entry);
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//            super.renderDecorations(guiGraphics, mouseX, mouseY);
        }
    }

    /**
     * One row in the left-hand list.
     * Extends ObjectSelectionList.Entry and implements render() + narration. :contentReference[oaicite:3]{index=3}
     */
    private static class DataListEntry extends ObjectSelectionList.Entry<DataListEntry> {

        private final DataListWidget list;
        private final DataSelectionScreen parent;
        private final ListEntryData data;

        public DataListEntry(DataListWidget list,
                             DataSelectionScreen parent,
                             ListEntryData data) {
            this.list = list;
            this.parent = parent;
            this.data = data;
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

            Font font = this.parent.font;

            boolean selected = this.list.getSelected() == this;

            // Highlight selected/hovered rows
            if (hovered || selected) {
                int bg = selected ? 0x80FFFFFF : 0x40FFFFFF;
                gfx.fill(left, top, left + width, top + height, bg);
            }

            int x = left + 4;
            int y = top + 4;

            // Optional icon
            if (!this.data.icon().isEmpty()) {
                gfx.renderItem(this.data.icon(), x, y);
                x += 20;
            }

            // Title (single line)
            gfx.drawString(font, this.data.title(), x, y, 0x000000, false);

            // Subtitle in smaller gray text (clamped to row width)
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
        public Component getNarration() {
            // Narrator text when navigating with accessibility tools
            return Component.translatable(
                    "narrator.yourmodid.data_entry",
                    this.data.title(),
                    this.data.subtitle()
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                this.list.setSelected(this);
                this.parent.setSelected(this.data);
                return true;
            }
            return false;
        }
    }

    /**
     * Convenience helper for opening this screen.
     * Call this on the **client** side (e.g. from a keybinding or button).
     */
    public static void open(List<ListEntryData> entries) {
        Minecraft.getInstance().setScreen(new DataSelectionScreen(entries));
    }
}
