//package io.github.daxigua2333.mocai_clues.guis;
//
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.ObjectSelectionList;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.FastColor;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ExampleListScreen extends Screen {
//
//    private static final int LIST_TOP = 32;
//    private static final int LIST_BOTTOM_MARGIN = 40;
//    private static final int ROW_HEIGHT = 24;
//
//    private ExampleListWidget listWidget;
//    private Button addButton;
//
//    private final List<RowData> rows = new ArrayList<>();
//
//    public ExampleListScreen() {
//        super(Component.literal("Example List"));
//        // Seed with some sample data
//        rows.add(defaultRow("First row"));
//        rows.add(defaultRow("Second row"));
//    }
//
//    @Override
//    protected void init() {
//        super.init();
//
//        int listBottom = this.height - LIST_BOTTOM_MARGIN;
//
//        // Create and register the list as a widget (1.21.x lists extend AbstractWidget)
//        this.listWidget = this.addRenderableWidget(
//            new ExampleListWidget(
//                this.minecraft,
//                this.width,
//                this.height,
//                LIST_TOP,
//                listBottom,
//                ROW_HEIGHT
//            )
//        );
//
//        this.listWidget.setRows(this.rows);
//
//        // Button at the bottom to add a row with default data
//        this.addButton = this.addRenderableWidget(
//            new Button.Builder(
//                Component.literal("Add row"),
//                button -> {
//                    RowData newRow = defaultRow("New row " + (rows.size() + 1));
//                    rows.add(newRow);
//                    listWidget.setRows(rows);
//
////                    // Optionally select the new row
////                    if (!listWidget.children().isEmpty()) {
////                        listWidget.setSelected(listWidget.children().get(listWidget.getItemCount() - 1));
////                    }
//                }
//            )
//                .bounds(this.width / 2 - 50, listBottom + 8, 100, 20) // x, y, width, height :contentReference[oaicite:4]{index=4}
//                .build()
//        );
//    }
//
//    private RowData defaultRow(String name) {
//        // Example: no icon by default, just text.
//        // Replace with your own default icon + texts.
//        return new RowData(
//            null,
//            Component.literal(name),
//            Component.literal("Secondary text")
//        );
//    }
//
//    @Override
//    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
//        super.render(guiGraphics, mouseX, mouseY, partialTick);
//
//        // Title
//        guiGraphics.drawCenteredString(
//            this.font,
//            this.title,
//            this.width / 2,
//            15,
//            0xFFFFFFFF // ARGB; safe for 1.21.1 and the 1.21.6+ behavior that expects alpha. :contentReference[oaicite:5]{index=5}
//        );
//    }
//
//    @Override
//    public boolean isPauseScreen() {
//        return false;
//    }
//
//    // -------------------------------------------------------------------------
//    // The actual scrollable list widget
//    // -------------------------------------------------------------------------
//
//    public class ExampleListWidget extends ObjectSelectionList<ExampleListWidget.Entry> {
//
//        public ExampleListWidget(Minecraft minecraft, int screenWidth, int screenHeight,
//                                 int top, int bottom, int itemHeight) {
//            // AbstractSelectionList constructor signature is still the classic
//            // (minecraft, width, height, top, bottom, itemHeight) in 1.21.1.
//            super(minecraft, screenWidth, screenHeight, top, itemHeight);
//        }
//
//        public void setRows(List<RowData> newRows) {
//            this.clearEntries();
//            for (RowData data : newRows) {
//                this.addEntry(new Entry(data));
//            }
//        }
//
//        @Nullable
//        public RowData getSelectedRow() {
//            Entry entry = this.getSelected();
//            return entry != null ? entry.data : null;
//        }
//
//        // One row in the list --------------------------------------------------
//        public class Entry extends ObjectSelectionList.Entry<Entry> {
//
//            private final RowData data;
//
//            public Entry(RowData data) {
//                this.data = data;
//            }
//
//            @Override
//            public void render(
//                GuiGraphics guiGraphics,
//                int index,
//                int top,
//                int left,
//                int width,
//                int height,
//                int mouseX,
//                int mouseY,
//                boolean hovering,
//                float partialTick
//            ) {
//                // Highlight background if selected
//                if (ExampleListWidget.this.getSelected() == this) {
//                    // semi-transparent highlight
//                    guiGraphics.fill(left, top, left + width, top + height, FastColor.ARGB32.color(128, 255, 255, 255));
//                } else if (hovering) {
//                    guiGraphics.fill(left, top, left + width, top + height, FastColor.ARGB32.color(64, 255, 255, 255));
//                }
//
//                int x = left + 4;
//                int centerY = top + height / 2;
//
//                // Optional icon
//                if (data.icon() != null) {
//                    // 16x16 icon using GuiGraphics.blit(ResourceLocation, x, y, u, v, w, h) :contentReference[oaicite:7]{index=7}
//                    guiGraphics.blit(data.icon(), x, centerY - 8, 0, 0, 16, 16);
//                    x += 20; // Leave some space after the icon
//                }
//
//                // Primary text
//                guiGraphics.drawString(
//                    Minecraft.getInstance().font,
//                    data.primary(),
//                    x,
//                    centerY - 6,
//                    0xFFFFFFFF,
//                    false
//                );
//
//                // Optional secondary text (smaller and grey-ish)
//                if (data.secondary() != null) {
//                    guiGraphics.drawString(
//                        Minecraft.getInstance().font,
//                        data.secondary(),
//                        x,
//                        centerY + 4,
//                        0xFFAAAAAA,
//                        false
//                    );
//                }
//            }
//
//            @Override
//            public Component getNarration() {
//                // Basic narration; you can expand this as needed.
//                if (data.secondary() != null) {
//                    return Component.literal("")
//                        .append(data.primary())
//                        .append(Component.literal(" - "))
//                        .append(data.secondary());
//                }
//                return data.primary();
//            }
//        }
//    }
//}
