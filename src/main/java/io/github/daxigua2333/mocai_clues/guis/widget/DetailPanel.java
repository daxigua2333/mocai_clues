package io.github.daxigua2333.mocai_clues.guis.widget;

import com.mojang.blaze3d.vertex.Tesselator;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
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
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/**
 * a scrollable container for nested widgets
 * mainly pay attention to 4 methods:
 * constructor, getContentHeight, drawPanel (postion the widgets), children (for internally input handling)
 * */
public class DetailPanel extends ScrollPanel {

    @Nullable
    private ClueObject object = null;
    private ClueObject copy;
    private List<AbstractWidget> children = new ArrayList<>();
    private final int spacing = 6;  // spacing between each widgets

    private float lastPartialTick;

    private final Button applyButton;
    private final Button editButton;
    private final Button deleteButton;
    private final Button cancelButton;

    private boolean dirty = false;
    public void setDirty() {
        this.dirty = true;
    }

    public DetailPanel(Minecraft mc, int width, int height, int top, int left) {
        super(mc, width, height, top, left, 0, 6 , -16777216, -8355712, -4144960);  // TODO: bg...etc

        Font font = Minecraft.getInstance().font;

        // top buttons  TODO: cancel, delete
        this.applyButton = Button.builder(Component.translatable("apply"), btn -> {
            PacketDistributor.sendToServer(new ClueObjectUpsertPayload(copy));
            this.setState(State.READONLY);
        }).bounds(0, 0, 0, 20).build();
        this.cancelButton = Button.builder(Component.translatable("cancel"), btn -> {
            this.setState(State.READONLY);
        }).bounds(0, 0, 0, 20).build();

        this.editButton = Button.builder(Component.translatable("edit"), btn -> {
            if (object == null) return;  // actually buttons won't be built if object is null
            this.setState(State.EDIT);
        }).bounds(0, 0, 0, 20).build();

        this.deleteButton = Button.builder(Component.translatable("delete"), btn -> {
            if (object == null) return;
            ConfirmScreen confirm = new ConfirmScreen(
                (BooleanConsumer) confirmed -> {
                    mc.popGuiLayer();
                    if (confirmed) {
                        PacketDistributor.sendToServer(new ClueObjectDeletePayload(object.getId()));
                        this.updateObject(null);
                    }
                },
                Component.translatable("gui.mymod.confirm_delete.title"),
                Component.translatable("gui.mymod.confirm_delete.body"),
                Component.translatable("gui.mymod.delete"),
                CommonComponents.GUI_CANCEL
            );

            // Optional: disable buttons for N ticks to prevent misclicks.
            confirm.setDelay(10);

            mc.pushGuiLayer(confirm);
        }).bounds(0, 0, 0, 20).build();

        setDirty();
    }

    /** editor mode or not */
    enum State {
        EDIT, READONLY, EMPTY;
        public State next() {
            State[] vals = values();
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }
    private State state = State.READONLY;
    public State getState() {
        return state;
    }
    public void setState(State s) {
        this.state = s;
        this.setDirty();
        switch (s) {
            case EDIT -> {
                copy = object == null ? null : object.clone();
            }
            case READONLY -> {
                copy = null;
            }
            case null, default -> throw new RuntimeException("inaccessible");
        }
    }

    /** outer update obj */
    public void updateObject(@Nullable ClueObject obj) {
        if (obj == null && this.object == null) return;
        if (obj != null && obj.equals(this.object)) return;
        this.object = obj;
        this.setDirty();
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
            rebuild();
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

    private void rebuild() {
        List<AbstractWidget> details = new ArrayList<>();

        switch (state) {
            case EDIT -> {
                if (copy == null) break;
                details.add(applyButton);
                details.add(cancelButton);
                for (ClueComponent component : copy.getComponents()) {
                    List<AbstractWidget> editables = component.getEditable();
                    if (editables != null) {
                        details.addAll(editables);
                    }
                }
            }
            case READONLY -> {
                if (object == null) break;
                details.add(deleteButton);
                details.add(editButton);
                for (ClueComponent component : object.getComponents()) {  // use copy
                    List<AbstractWidget> uneditables = component.getUneditable();
                    if (uneditables != null) {
                        details.addAll(uneditables);
                    }
                }
            }
            case null, default -> throw new RuntimeException("why");
        }

        this.children = details;
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
