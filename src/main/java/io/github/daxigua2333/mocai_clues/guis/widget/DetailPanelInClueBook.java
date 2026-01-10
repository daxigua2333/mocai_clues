package io.github.daxigua2333.mocai_clues.guis.widget;

import com.mojang.blaze3d.vertex.Tesselator;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.gui.FlexibleContainer;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectDeletePayload;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectUpsertPayload;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DetailPanelInClueBook extends FlexibleContainer {

    @Nullable
    private ClueObject object = null;

    private final List<AbstractWidget> header = new ArrayList<>();
    private final PlayerSender sender;
    private final ScrollPage page;


    @Override
    protected void markDirty() {
        super.markDirty();
        if (page == null) return;
        page.markDirty();
    }

    @Override
    protected void processDirty() {
        // rebuild header
//        switch (state) {
//            case EDIT -> {
//                header.clear();
//                header.addAll(List.of(applyButton, cancelButton));
//            }
//            case READONLY -> {
//                header.clear();
//                header.addAll(List.of(deleteButton, editButton));
//            }
//        }
        // rebuild page
        page.processDirty();
    }

    @Override
    protected void reLayout() {

    }

    public DetailPanelInClueBook(Minecraft mc, int width, int height, int top, int left) {
        super(left, top, width, height, Component.empty());

        page = new ScrollPage(mc, width, height, top, left);

        // init header
        int y = top - 20;
        sender = new PlayerSender(left, y, 10, 110, 20, info -> {
            // TODO: send packet, server attach
            MoCaiClues.LOGGER.debug("{}", info);
        });
        header.add(sender);
    }


    /** outer update obj */
    public void updateObject(@Nullable ClueObject obj) {
        if (obj == null && this.object == null) return;
        if (obj != null && obj.equals(this.object)) return;
        this.object = obj;
        this.markDirty();
    }


    @Override
    protected void renderTick(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        for (var w : header) {
            w.render(graphics, mouseX, mouseY, partialTick);
        }

        page.render(graphics, mouseX, mouseY, partialTick);

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> result = new ArrayList<>(header);
        result.add(page);
        return result;
    }


    class ScrollPage extends ScrollPanel {
        private final List<AbstractWidget> children = new ArrayList<>();
        private final int spacing = 6;  // spacing between each widgets

        private float lastPartialTick;

        private boolean dirty = false;
        public void markDirty() {
            this.dirty = true;
        }

        public ScrollPage(Minecraft mc, int width, int height, int top, int left) {
            super(mc, width, height, top, left, 0, 6 , -16777216, -8355712, -4144960);  // TODO: bg...etc

            Font font = Minecraft.getInstance().font;

            markDirty();
        }

        /** the entire content height (which means can exceed the screen height) */
        @Override
        protected int getContentHeight() {
            int result = 0;
            for (var widget : this.children) {
                result += widget.getHeight() + this.spacing;
            }
            if (result < this.height) {  // disable the default behavior when ContentHeight is smaller than panel height
                result = height;
            }
            return result;
        }

        /** invoked each tick. Mainly position widgets here.
         * relativeY: y value which has counted the scrollDistance
         * */
        @Override
        protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess,
                                 int mouseX, int mouseY) {
            if (this.dirty) {
                processDirty();
                this.dirty = false;
            }

            // re layout and render
            int x = this.left + this.border;
            int y = relativeY;
    //        MoCaiClues.LOGGER.debug("{}", relativeY);  // after each scroll it goes into 6, -14, -34

            for (var w : this.children) {
                w.setX(x);
                w.setY(y);
                w.setWidth(this.width);
                w.render(guiGraphics, mouseX, mouseY, this.lastPartialTick);  // update height is in this tick()
                y += w.getHeight() + this.spacing;
            }
        }

        private void processDirty() {
            this.children.clear();

            if (object == null) return;
            for (ClueComponent component : object.getComponents()) {  // use copy
                List<AbstractWidget> uneditables = component.getUneditable();
                if (uneditables != null) {
                    this.children.addAll(uneditables);
                }
            }

        }

        /**
         * will internally go through this to handle input (mouse click/release/drag/scrolled)
         */
        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }


        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {

        }

        // record partial tick, so we can use it inside drawPanel()
        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.lastPartialTick = partialTick;
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // no background
        @Override
        protected void drawBackground(GuiGraphics guiGraphics, Tesselator tess, float partialTick) {
    //        Screen.renderMenuBackgroundTexture(guiGraphics, Screen.MENU_BACKGROUND, this.left, this.top, 0.0F, 0.0F, this.width, this.height);
        }

    }
}
