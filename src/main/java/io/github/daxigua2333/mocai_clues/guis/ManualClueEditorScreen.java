package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.DetailPanelNew;
import io.github.daxigua2333.mocai_clues.networks.ManualClueCreatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ManualClueEditorScreen extends ClueBookScreenLayout {
    private final Supplier<List<ClueObject>> clueSupplier;
    private final Consumer<ClueObject> applyChange;
    private final Consumer<ClueObject> deleteCurrent;

    private static final int ENTRY_HEIGHT = 20;


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
        super(Component.translatable(MoCaiClues.MODID + "screen.manual_clue"));
        this.clueSupplier = clueSupplier;
        this.applyChange = applyChange;
        this.deleteCurrent = deleteCurrent;
    }

    // ===== build and add widgets =======
    @Override
    protected void init() {
        super.init();

        // build widgets
        int left = (this.width - WHOLE_W) / 2;
        int top = (this.height - WHOLE_H) / 2;

        Button createButton = Button.builder(Component.literal("+"), btn -> {
            PacketDistributor.sendToServer(new ManualClueCreatePayload());
        }).bounds(
                left + LIST_X_OFFSET + LIST_W - 28,
                top + LIST_Y_OFFSET + 11,
                16, 16
        ).build();

        var details = new DetailPanelNew(
                Minecraft.getInstance(),
                PANEL_W - 28,
                PANEL_H - 48,
                top + PANEL_Y_OFFSET + 34,
                left + PANEL_X_OFFSET + 15,
                applyChange,
                deleteCurrent
        );

        var list = new AutoUpdatedScrollableListWidget<>(
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
                Component.translatable("mocai_clues.screen.manual_clue").getString(),
                LIST_W-40);
        gfx.drawString(Minecraft.getInstance().font,
                clippedText,
                left + LIST_X_OFFSET+13, top + LIST_Y_OFFSET+15, 0x000000);
    }
}
