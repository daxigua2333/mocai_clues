//package io.github.daxigua2333.mocai_clues.guis.widget;
//
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.networks.ClueObjectHolderDeltaSyncPayload;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.ObjectSelectionList;
//import net.minecraft.network.chat.Component;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.function.Supplier;
//
//public class AutoUpdatedScrollableMapWidget extends ObjectSelectionList<AutoUpdatedScrollableMapWidget.Entry> {
//
//    private final Supplier<Map<UUID, ClueObject>> mapSupplier;
//    private final Function<ClueObject, Component> labelMapper;
//    private final Consumer<ClueObject> clickHandler;
//    private final int z;
//
//    private UUID selectedID;
//    private Map<UUID, ClueObject> former;
//
//
//    // ====== constructor =======
//    public AutoUpdatedScrollableMapWidget(Minecraft minecraft,
//                                          int x, int y, int z,
//                                          int width, int height,
//                                          int itemHeight,
//                                          Supplier<Map<UUID, ClueObject>> mapSupplier,
//                                          Function<ClueObject, Component> labelMapper,
//                                          Consumer<ClueObject> clickHandler) {
//        super(minecraft, width, height, y, itemHeight);
//
//        this.mapSupplier = mapSupplier;
//        this.labelMapper = labelMapper;
//        this.clickHandler = clickHandler;
//        this.z = z;
//
//        // Position widget and inform layout system
//        this.setX(x);
//        this.setY(y); // TODO: ?
////        this.updateSizeAndPosition(x, y, width, height);
////        this.updateSizeAndPosition(width, height, y);
//    }
//
//    // ========= sync part =======
//    @Override
//    public void setSelected(@Nullable AutoUpdatedScrollableMapWidget.Entry selected) {
//        super.setSelected(selected);
//        if (selected != null) {
//            this.selectedID = selected.value.getId();
//        }
//    }
//
//    private void syncIfNeeded() {
//        List<ClueObjectHolderDeltaSyncPayload> deltas = deltaSupplier.get();
//        if (!deltas.isEmpty()) {
//            // apply deltas
//            for (var delta : deltas) {
//                switch (delta.deltaType()) {
//                    case PUT -> map.put(delta.id(), delta.object());
//                    case REMOVE -> map.remove(delta.id());
//                    case CLEAR -> map.clear();
//                    case null, default -> throw new NullPointerException("no....>_<...please no....");
//                }
//            }
//            deltas.clear();
//
//        Map<UUID, ClueObject> currentMap = mapSupplier.get();
//        if (currentMap.equals())
//
//            // create new entries, find former selected one (if exists)
//            List<Entry> entries = new ArrayList<>(map.size());
//            Entry selected = null;
//            for (var obj : map.values()) {
//                Entry newEntry = new Entry(obj);
//                if (this.selectedID == obj.getId()) {selected = newEntry;}
//                entries.add(newEntry);
//            }
//            // update entries
//            this.replaceEntries(entries);
//            this.setSelected(selected);
//        }
//    }
//
//    @Override
//    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
//        // auto update every tick
//        syncIfNeeded();
//        // manage z
//        var pose = graphics.pose();
//        pose.pushPose();
//        pose.translate(0,0,z);
//        // render
//        super.renderWidget(graphics, mouseX, mouseY, partialTick);
//        pose.popPose();
//    }
//
//
//
//    // ====== appearance ======
//    @Override
//    public int getRowWidth() {
//        return this.getWidth();
////        Make rows slightly narrower than the full width so the scrollbar has space
////        return this.width - 8;
//    }
//    @Override
//    protected int getScrollbarPosition() {
//        // Right edge of the list; SCROLLBAR_WIDTH is handled internally.
//        return this.getX() + this.getWidth() - 6;
//    }
//    @Override
//    protected void renderListBackground(GuiGraphics guiGraphics) {
////        super.renderListBackground(guiGraphics);
//    }
//
//    @Override
//    protected void renderDecorations(GuiGraphics guiGraphics, int mouseX, int mouseY) {
////        super.renderDecorations(guiGraphics, mouseX, mouseY);
//    }
//
//
//    /** One row in the list. */
//    public class Entry extends ObjectSelectionList.Entry<Entry> {
//
//        private final ClueObject value;
//        private final Component label;
//
//        public Entry(ClueObject value) {
//            this.value = value;
//            this.label = labelMapper.apply(value);
//        }
//
//        @Override
//        public void render(GuiGraphics gfx,
//                           int index,
//                           int top,
//                           int left,
//                           int width,
//                           int height,
//                           int mouseX,
//                           int mouseY,
//                           boolean hovered,
//                           float partialTick) {
//
//            Font font = Minecraft.getInstance().font;
//
//            boolean selected = AutoUpdatedScrollableMapWidget.this.getSelected() == this;
//
//
//            // Highlight selected/hovered rows
//            if (hovered || selected) {
//                int bg = selected ? 0x80FFFFFF : 0x40FFFFFF;
//                gfx.fill(left, top, left + width, top + height, bg);
//            }
//
//            int x = left + 4;
//            int y = top + 4;
//
//            // Title (single line)
//            gfx.drawString(font, label, x, y, 0x000000, false);
//
////            // Optional icon
////            if (!this.value.icon().isEmpty()) {
////                gfx.renderItem(this.value.icon(), x, y);
////                x += 20;
////            }
////
////            // Subtitle in smaller gray text (clamped to row width)
////            String subtitleText = this.data.subtitle().getString();
////            if (!subtitleText.isEmpty()) {
////                int maxWidth = width - (x - left) - 4;
////                String trimmed = font.plainSubstrByWidth(subtitleText, maxWidth);
////                gfx.drawString(font, trimmed,
////                        x,
////                        y + font.lineHeight + 1,
////                        0xFFA0A0A0,
////                        false
////                );
////            }
//
//        }
//
//        @Override
//        public boolean mouseClicked(double mouseX, double mouseY, int button) {
//            if (button == 0) { // left click
//                // Visually select the row
//                AutoUpdatedScrollableMapWidget.this.setSelected(this);
//                // Fire your callback
//                clickHandler.accept(value);
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public Component getNarration() {
//            return label;
//        }
//    }
//}
