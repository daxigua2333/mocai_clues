package io.github.daxigua2333.mocai_clues.guis.widget;

import com.mojang.blaze3d.vertex.Tesselator;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.gui.FlexibleContainer;
import io.github.daxigua2333.mocai_clues.mixins.ScrollPanelAccessor;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DetailPanelNew extends FlexibleContainer {

    @Nullable
    private ClueObject object = null;
    @Nullable
    private ClueObject copy;

    private final List<AbstractWidget> header = new ArrayList<>(2);
    private ScrollPage page;

    private final Button applyButton;
    private final Button editButton;
    private final Button deleteButton;
    private final Button cancelButton;


    public DetailPanelNew(Minecraft mc, int width, int height, int top, int left,
                          Consumer<ClueObject> applyChange,
                          Consumer<ClueObject> deleteCurrent) {
        super(left, top, width, height, Component.empty());

        page = new ScrollPage(mc, width, height, top, left);

        // top buttons
        int y = top - 25;
        this.applyButton = Button.builder(Component.translatable(MoCaiClues.MODID + ".screen.apply"), btn -> {
            applyChange.accept(copy);
            this.setState(State.READONLY);
        }).bounds(left + width - 80, y, 40, 20).build();
        this.cancelButton = Button.builder(Component.translatable(MoCaiClues.MODID + ".screen.cancel"), btn -> {
            this.setState(State.READONLY);
        }).bounds(left + width - 40, y, 40, 20).build();

        this.editButton = Button.builder(Component.translatable(MoCaiClues.MODID + ".screen.edit"), btn -> {
            if (object == null) return;  // actually buttons won't be built if object is null
            this.setState(State.EDIT);
        }).bounds(left + width - 40, y, 40, 20).build();

        this.deleteButton = Button.builder(Component.translatable(MoCaiClues.MODID + ".screen.delete"), btn -> {
            if (object == null) return;
            ConfirmScreen confirm = new ConfirmScreen(
                    (BooleanConsumer) confirmed -> {
                        mc.popGuiLayer();
                        if (confirmed) {
                            deleteCurrent.accept(object);
                            this.updateObject(null);
                        }
                    },
                    Component.translatable(MoCaiClues.MODID + ".screen.confirm_delete.title"),
                    Component.translatable(MoCaiClues.MODID + ".screen.confirm_delete.body"),
                    Component.translatable(MoCaiClues.MODID + ".screen.delete"),
                    CommonComponents.GUI_CANCEL
            );

            // Optional: disable buttons for N ticks to prevent misclicks.
            confirm.setDelay(10);

            mc.pushGuiLayer(confirm);
        }).bounds(left, y, 40, 20).build();


        // dirty
        markDirty();
    }

    /**
     * editor mode or not
     */
    enum State {
        EDIT, READONLY, EMPTY;

        public State next() {
            State[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    private State state = State.EMPTY;

    public State getState() {
        return state;
    }

    public void setState(State s) {
        this.state = s;
        this.markDirty();
        switch (s) {
            case EDIT -> {
                copy = object == null ? null : object.clone();
            }
            case READONLY -> {
                copy = null;
            }
            case EMPTY -> {
                object = null;
                copy = null;
                header.clear();
            }
            case null, default -> throw new RuntimeException("inaccessible");
        }
    }

    /**
     * outer update obj
     */
    public void updateObject(@Nullable ClueObject obj) {
        if (obj == null && this.object == null) return;
        if (obj != null && obj.equals(this.object)) return;
        this.object = obj;
        this.setState(State.READONLY);
//        this.setDirty();
    }


    @Override
    protected void markDirty() {
        super.markDirty();
        if (page != null) {
            page.markDirty();
        }
    }


    @Override
    protected void processDirty() {
        // rebuild header
        switch (state) {
            case EDIT -> {
                header.clear();
                header.addAll(List.of(applyButton, cancelButton));
            }
            case READONLY -> {
                header.clear();
                header.addAll(List.of(deleteButton, editButton));
            }
        }
        // rebuild page
//        page.processDirty();

    }

    @Override
    protected void reLayout() {
        int x = getX();
        int y = getY();
        int w = getWidth();
        applyButton.setPosition(x + w - 80, y - 25);
        cancelButton.setPosition(x + w - 40, y - 25);
        editButton.setPosition(x + w - 40, y - 25);
        deleteButton.setPosition(x, y - 25);

        page = new ScrollPage(Minecraft.getInstance(), width, height, y, x);
//        page.setPosition(x, y);  // TODO: idk why this is useless
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
            super(mc, width, height, top, left, 0, 6, -16777216, -8355712, -4144960);  // TODO: bg...etc

            Font font = Minecraft.getInstance().font;

            markDirty();
        }

        // ========== re-layout =============
        public void setPosition(int x, int y) {
            var accessor = (ScrollPanelAccessor) this;
            accessor.setX(x);
            accessor.setY(y);
        }

        /**
         * the entire content height (which means can exceed the screen height)
         */
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

        /**
         * invoked each tick. Mainly position widgets here.
         * relativeY: y value which has counted the scrollDistance
         */
        @Override
        protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess,
                                 int mouseX, int mouseY) {
            if (this.dirty) {
                processDirty();
                this.dirty = false;
            }

            // re layout(**has nothing to do with dirty**) and render
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

            switch (state) {
                case EDIT -> {
                    if (copy == null) break;
                    for (ClueComponent component : copy.getComponents()) {
                        List<AbstractWidget> editables = component.getEditable();
                        if (editables != null) {
                            this.children.addAll(editables);
                        }
                    }
                }
                case READONLY -> {
                    if (object == null) break;
                    for (ClueComponent component : object.getComponents()) {  // use copy
                        List<AbstractWidget> uneditables = component.getUneditable();
                        if (uneditables != null) {
                            this.children.addAll(uneditables);
                        }
                    }
                }
                case EMPTY -> {
                }
                case null, default -> throw new RuntimeException("why");
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
