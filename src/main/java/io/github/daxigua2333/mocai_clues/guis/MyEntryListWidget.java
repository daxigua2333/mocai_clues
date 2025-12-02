//package io.github.daxigua2333.mocai_clues.guis;
//
//
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.ObjectSelectionList;
//import net.minecraft.network.chat.Component;
//
//import javax.annotation.Nullable;
//import java.util.List;
//
//public class MyEntryListWidget extends ObjectSelectionList<MyEntryListWidget.Entry> {
//
//    public MyEntryListWidget(Minecraft mc,
//                             int width,
//                             int height,
//                             int top,
//                             int bottom,
//                             int itemHeight) {
//
//        super(mc, width, height, top, itemHeight);
//        this.setRenderBackground(false);
//        this.setRenderTopAndBottom(false);
//    }
//
//    /** Helper for filling the list from data. */
//    public void setEntries(List<MyEntryData> data) {
//        this.clearEntries();
//        for (MyEntryData d : data) {
//            this.addEntry(new Entry(this, d));
//        }
//    }
//
//    @Override
//    public int getRowWidth() {
//        // Use full list width
//        return this.width;
//    }
//
//    @Override
//    protected int getScrollbarPosition() {
//        // Right side of the list
//        return this.getRowLeft() + this.width - 6;
//    }
//
//    // --- Entry class (one row) ---
//
//    public static class Entry extends ObjectSelectionList.Entry<Entry> {
//
//        private final MyEntryListWidget parent;
//        private final MyEntryData data;
//
//        public Entry(MyEntryListWidget parent, MyEntryData data) {
//            this.parent = parent;
//            this.data = data;
//        }
//
//        public MyEntryData getData() {
//            return data;
//        }
//
//        @Override
//        public void render(
//                GuiGraphics guiGraphics,
//                int index,
//                int top,
//                int left,
//                int width,
//                int height,
//                int mouseX,
//                int mouseY,
//                boolean hovered,
//                float partialTick
//        ) {
//            Font font = Minecraft.getInstance().font;
//
//            // Background highlight on hover/selected
//            boolean selected = (parent.getSelected() == this);
//            if (hovered || selected) {
//                int color = selected ? 0x80FFFFFF : 0x40FFFFFF;
//                guiGraphics.fill(left, top, left + width, top + height, color);
//            }
//
//            // Title
//            guiGraphics.drawString(
//                    font,
//                    data.title(),
//                    left + 4,
//                    top + 4,
//                    0xFFFFFF,
//                    false
//            );
//
//            // Short info
//            guiGraphics.drawString(
//                    font,
//                    data.shortInfo(),
//                    left + 4,
//                    top + 4 + 10,
//                    0xAAAAAA,
//                    false
//            );
//        }
//
//        @Override
//        public boolean mouseClicked(double mouseX, double mouseY, int button) {
//            if (button == 0) {
//                parent.setSelected(this); // updates list selection
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public Component getNarration() {
//            return data.title();
//        }
//    }
//}
