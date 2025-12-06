package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.DropdownWidget;
import io.github.daxigua2333.mocai_clues.networks.ManualClueCreatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class WandScreen extends Screen {
//    private final List<ListEntryData> entries;
    private Supplier<List<ClueType>> typeSupplier;
    private Function<ClueType, List<ClueObject>> clueSupplier;

    private DropdownWidget<ClueType> tab;
    private AutoUpdatedScrollableListWidget<ClueObject> list;
//    private DataSelectionScreen.ListEntryData selected;

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
    private static final int LIST_HEIGHT = 214 - LIST_Y_OFFSET;
    private static final int SPLIT_X_OFFSET = 97;
    private static final int ENTRY_HEIGHT = 20;


    // ====== constructor, and open static =======
    public static void open(
            Supplier<List<ClueType>> typeSupplier,
            Function<ClueType, List<ClueObject>> clueSupplier
    ) {
//        List<ClueObject> list = List.of(new ClueObject(ClueType.MANUAL), new ClueObject(ClueType.MANUAL), new ClueObject(ClueType.MANUAL), new ClueObject(ClueType.MANUAL), new ClueObject(ClueType.MANUAL), new ClueObject(ClueType.MANUAL), new ClueObject(ClueType.MANUAL), new ClueObject(ClueType.MANUAL), new ClueObject(ClueType.MANUAL));
        Minecraft.getInstance().setScreen(new WandScreen(
//                () -> List.of(ClueType.MANUAL, ClueType.FOOTPRINT, ClueType.FOOTPRINT1, ClueType.FOOTPRINT2, ClueType.FOOTPRINT3, ClueType.FOOTPRINT4, ClueType.FOOTPRINT6, ClueType.FOOTPRINT7),
//                () -> list
                typeSupplier,
                clueSupplier
        ));
    }
    public WandScreen(
            Supplier<List<ClueType>> typeSupplier,
            Function<ClueType, List<ClueObject>> clueSupplier
    ) {
        super(Component.translatable("screen." + MoCaiClues.MODID + ".data_selection"));
        this.typeSupplier = typeSupplier;
        this.clueSupplier = clueSupplier;
    }

    // ===== build and add widgets =======
    @Override
    protected void init() {
        super.init();

        this.clearWidgets();
        // build and add widgets
        this.list = new AutoUpdatedScrollableListWidget<ClueObject>(
                this.minecraft,
                (this.width - TEXTURE_WIDTH) / 2 + LIST_X_OFFSET,
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET,
                1,
                LIST_WIDTH, LIST_HEIGHT,
                ENTRY_HEIGHT,
                () -> this.clueSupplier.apply(ClueType.MANUAL),  // TODO
                (obj) -> obj.getId(),
//                Component::literal,
                (clue) -> Component.literal(clue.getId().toString()),  // TODO
                (clueObject) -> {  /// TODO

                }
        );

        this.tab = new DropdownWidget<>(   // TODO: test sync
                (this.width - TEXTURE_WIDTH) / 2 + LIST_X_OFFSET - 2,  // TODO: pos
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET - 16,
                2,
                60,
                16,
                0,
                this.typeSupplier,
//                Component::literal,
                (type) -> Component.literal(type.toString()),  // TODO
                (clueType) -> {   //// TODO
                    this.typeSupplier = () -> List.of(ClueType.MANUAL, ClueType.MANUAL, ClueType.MANUAL);
                }
        );

        Button createButton = Button.builder(Component.literal("+"), btn -> {
            PacketDistributor.sendToServer(new ManualClueCreatePayload());
        }).bounds(
                (this.width - TEXTURE_WIDTH) / 2 + LIST_X_OFFSET + 20,
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET - 16,
                60, 16
        ).build();

        // click event order
        this.addRenderableWidget(this.tab);
        this.addRenderableWidget(this.list);
        this.addRenderableWidget(createButton);

    }

//    void setSelected(DataSelectionScreen.ListEntryData data) {
//        this.selected = data;
//    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);

        int panelRight = (this.width + PANEL_WIDTH) / 2;
        int panelTop = (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET;
        int splitX = (this.width - TEXTURE_WIDTH) / 2 + SPLIT_X_OFFSET;

        // Let vanilla render widgets (including our list)
        super.render(gfx, mouseX, mouseY, partialTick);

//        // Detail widget on the right
//        if (this.selected != null) {
//            drawDetailPanel(gfx, this.selected, splitX, panelTop, panelRight);
//        } else {
//            Component hint = Component.translatable("screen.yourmodid.data_selection.hint");
//            gfx.drawString(this.font, hint, splitX + 8, panelTop + 8, 0xFFA0A0A0, false);
//        }
//
//        // Tooltips (e.g. from item icon)
//        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - TEXTURE_WIDTH) / 2;
        int top = (this.height - TEXTURE_HEIGHT) / 2;

        // Your book texture
        gfx.blit(BOOK_TEXTURE, left, top, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

}
