//package io.github.daxigua2333.mocai_clues.guis;
//
//package com.example.my_mod.client.screen;
//
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.GuiGraphics;
//import net.minecraft.client.gui.screens.Screen;
//import net.minecraft.network.chat.Component;
//
//import java.util.List;
//
//public class MyEntryScreen extends Screen {
//
//    private final List<MyEntryData> data;
//    private MyEntryListWidget listWidget;
//
//    // You can pass the data in, or fetch it from somewhere static/singleton
//    public MyEntryScreen(List<MyEntryData> data) {
//        super(Component.translatable("screen." + "mymodid" + ".my_entry_screen"));
//        this.data = data;
//    }
//
//    @Override
//    protected void init() {
//        super.init();
//
//        // Left list configuration
//        int listWidth = 120;           // width of the left panel
//        int listTop = 20;
//        int listBottom = this.height - 20;
//        int itemHeight = 24;
//
//        this.listWidget = new MyEntryListWidget(
//                Minecraft.getInstance(),
//                listWidth,
//                this.height,
//                listTop,
//                listBottom,
//                itemHeight
//        );
//
//        // Position it on the left side
//        int leftX = 20;
//        this.listWidget.setLeftPos(leftX);
//
//        // Fill entries
//        this.listWidget.setEntries(this.data);
//
//        // Register as a renderable + handle input
//        this.addRenderableWidget(this.listWidget);
//    }
//
//    @Override
//    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//        // Background
//        this.renderBackground(guiGraphics);
//
//        // This will render child widgets including the list
//        super.render(guiGraphics, mouseX, mouseY, partialTick);
//
//        // Draw title
//        guiGraphics.drawCenteredString(
//                this.font,
//                this.title,
//                this.width / 2,
//                8,
//                0xFFFFFF
//        );
//
//        // Right-side detail panel
//        renderDetails(guiGraphics);
//
//        // Tooltips
//        this.renderTooltip(guiGraphics, mouseX, mouseY);
//    }
//
//    private void renderDetails(GuiGraphics guiGraphics) {
//        MyEntryListWidget.Entry selected = this.listWidget.getSelected();
//        if (selected == null) {
//            return;
//        }
//
//        MyEntryData data = selected.getData();
//
//        // Compute starting x for the detail region (right of the list)
//        int detailX = this.listWidget.getLeft() + this.listWidget.getRowWidth() + 20;
//        int detailY = 30;
//        int maxWidth = this.width - detailX - 20;
//
//        // Title
//        guiGraphics.drawString(this.font, data.title(), detailX, detailY, 0xFFFFFF, false);
//
//        // Short info (maybe as a subtitle)
//        guiGraphics.drawString(this.font, data.shortInfo(), detailX, detailY + 12, 0xAAAAAA, false);
//
//        // Long description (wrapped)
//        guiGraphics.drawWordWrap(
//                this.font,
//                data.description(),
//                detailX,
//                detailY + 28,
//                maxWidth,
//                0xDDDDDD
//        );
//    }
//
//    @Override
//    public boolean isPauseScreen() {
//        return false;
//    }
//}
