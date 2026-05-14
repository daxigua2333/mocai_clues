package io.github.daxigua2333.cmagic_clue.guis;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.component.ClueType;
import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import io.github.daxigua2333.cmagic_clue.component.data.InfoData;
import io.github.daxigua2333.cmagic_clue.component.data.ItemClue;
import io.github.daxigua2333.cmagic_clue.data.location.factory.FromSavedDataSerializable;
import io.github.daxigua2333.cmagic_clue.guis.whitelist.ItemClueHolder;
import io.github.daxigua2333.cmagic_clue.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.cmagic_clue.guis.widget.container.DetailPanelNew;
import io.github.daxigua2333.cmagic_clue.networks.ScreenCreateDefaultCluePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ManualClueEditorScreen extends ClueBookScreenLayout implements ItemClueHolder {
    private final Supplier<List<ClueObject>> clueSupplier;
    private final Consumer<ClueObject> applyChange;
    private final Consumer<ClueObject> deleteCurrent;

    private static final int ENTRY_HEIGHT = 20;


    private Button createButton;
    private DetailPanelNew details;
    private AutoUpdatedScrollableListWidget<ClueObject> list;

    // ====== constructor, and open static =======
    public static void open(
            Supplier<List<ClueObject>> clueSupplier,
            Consumer<ClueObject> applyChange,
            Consumer<ClueObject> deleteCurrent
    ) {
        Minecraft.getInstance().setScreen(new ManualClueEditorScreen(clueSupplier, applyChange, deleteCurrent));
    }

    public ManualClueEditorScreen(
            Supplier<List<ClueObject>> clueSupplier,
            Consumer<ClueObject> applyChange,
            Consumer<ClueObject> deleteCurrent
    ) {
        super(Component.translatable(CMagicClue.MODID + "screen.manual_clue"));
        this.clueSupplier = clueSupplier;
        this.applyChange = applyChange;
        this.deleteCurrent = deleteCurrent;

//        // build widgets
//        int left = (this.width - WHOLE_W) / 2;
//        int top = (this.height - WHOLE_H) / 2;
//
//        createButton = Button.builder(Component.literal("+"), btn -> {
//            PacketDistributor.sendToServer(new ManualClueCreatePayload());
//        }).bounds(
//                left + LIST_X_OFFSET + LIST_W - 28,
//                top + LIST_Y_OFFSET + 11,
//                16, 16
//        ).build();
//
//        details = new DetailPanelNew(
//                Minecraft.getInstance(),
//                PANEL_W - 28,
//                PANEL_H - 48,
//                top + PANEL_Y_OFFSET + 34,
//                left + PANEL_X_OFFSET + 15,
//                applyChange,
//                deleteCurrent
//        );
//
//        list = new AutoUpdatedScrollableListWidget<>(
//                Minecraft.getInstance(),
//                left + LIST_X_OFFSET + 6,
//                top + LIST_Y_OFFSET + 30,
//                1,
//                LIST_W - 20, LIST_H - 45,
//                ENTRY_HEIGHT,
//                clueSupplier,
//                ClueObject::getId,
//                (clueObject) -> {
////                    Component.literal(clue.getId().toString());
//                    InfoData compo = clueObject.getComponent(ComponentType.INFO_DATA);
//                    if (compo == null) return Component.empty();
//                    return Component.literal(compo.getName());
//                },
//                (clueObject) -> {
//                    if (clueObject == null) return;
//                    details.updateObject(clueObject);
//                }
//        );
//
//        stackSlot = new ItemStackPickerWidget(40, 40, Component.empty(), this, ItemStack.EMPTY, stack -> MoCaiClues.LOGGER.debug("{}", stack));

    }

    // ===== build and add widgets =======
    @Override
    protected void init() {
        this.clearWidgets();
        super.init();

        // build widgets
        int left = (this.width - WHOLE_W) / 2;
        int top = (this.height - WHOLE_H) / 2;

        createButton = Button.builder(Component.literal("+"), btn -> {
            PacketDistributor.sendToServer(new ScreenCreateDefaultCluePayload(ClueType.MANUAL, new FromSavedDataSerializable()));
        }).bounds(
                left + LIST_X_OFFSET + LIST_W - 28,
                top + LIST_Y_OFFSET + 11,
                16, 16
        ).build();

        details = new DetailPanelNew(
                Minecraft.getInstance(),
                PANEL_W - 36,
                PANEL_H - 64,
                top + PANEL_Y_OFFSET + 40,
                left + PANEL_X_OFFSET + 20,
                applyChange,
                deleteCurrent
        );

        list = new AutoUpdatedScrollableListWidget<>(
                this.minecraft,
                left + LIST_X_OFFSET + 6,
                top + LIST_Y_OFFSET + 30,
                1,
                LIST_W - 20, LIST_H - 45,
                ENTRY_HEIGHT,
                clueSupplier,
                ClueObject::getId,
                (clueObject) -> {
//                    Component.literal(clue.getId().toString());
                    InfoData compo = clueObject.getComponent(ComponentType.INFO_DATA);
                    if (compo == null) return Component.empty();
                    return Component.literal(compo.getName());
                },
                (clueObject) -> {
                    if (clueObject == null) return;
                    details.updateObject(clueObject);
                }
        );


//        createButton.setPosition(left + LIST_X_OFFSET + LIST_W - 28, top + LIST_Y_OFFSET + 11);
//        details.setPosition(left + PANEL_X_OFFSET + 15, top + PANEL_Y_OFFSET + 34);
//        list.setPosition(left + LIST_X_OFFSET + 6, top + LIST_Y_OFFSET + 30);
//        stackSlot.setPosition(40, 40);


        // click event order
        this.addRenderableWidget(list);
        this.addRenderableWidget(createButton);
        this.addRenderableWidget(details);

    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gfx, mouseX, mouseY, partialTick);
        int left = (width - WHOLE_W) / 2;
        int top = (height - WHOLE_H) / 2;
        String clippedText = font.plainSubstrByWidth(
                Component.translatable(CMagicClue.MODID + ".screen.manual_clue").getString(),
                LIST_W - 40);
        gfx.drawString(Minecraft.getInstance().font,
                clippedText,
                left + LIST_X_OFFSET + 13, top + LIST_Y_OFFSET + 15, 0x000000);
    }

    // TODO: very bad code but idk what else I can do..
    @Override
    public void setItemClue(ItemStack itemStack) {
//        for (var w : details.getPage().children()) {
//            if (w instanceof )
//        }
        // update copy
        ClueObject old = details.getCopy();
        ((ItemClue) old.getComponent(ComponentType.ITEM_CLUE)).setStack(itemStack);
        details.updateCopy(old);
    }
}
