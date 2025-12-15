package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.gui.editable.EditBoxRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.ReadOnlyDetailWidget;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.ScaledTextRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.SplitLineRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.TextListWithIndexRow;
import io.github.daxigua2333.mocai_clues.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.DetailPanel;
import io.github.daxigua2333.mocai_clues.guis.widget.DropdownWidget;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectSyncPayload;
import io.github.daxigua2333.mocai_clues.networks.ManualClueCreatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class WandScreen extends Screen {
//    private final List<ListEntryData> entries;
    private Supplier<List<ClueType>> typeSupplier;
    private Function<ClueType, List<ClueObject>> clueSupplier;

    private DropdownWidget<ClueType> tab;
    private AutoUpdatedScrollableListWidget<ClueObject> list;
    private DetailPanel details;
//    private DataSelectionScreen.ListEntryData selected;

    private static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "textures/gui/casebook.png");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_X_OFFSET = 15;
    private static final int PANEL_Y_OFFSET = 43;
    private static final int PANEL_WIDTH = 243 - PANEL_X_OFFSET;
    private static final int PANEL_HEIGHT = 222 - PANEL_Y_OFFSET;
    private static final int LIST_X_OFFSET = 22;
    private static final int LIST_Y_OFFSET = 56;
    private static final int LIST_WIDTH = 89 - LIST_X_OFFSET;
    private static final int LIST_HEIGHT = 214 - LIST_Y_OFFSET;
    private static final int ENTRY_HEIGHT = 20;
    private static final int SPLIT_X_OFFSET = 97;
    private static final int DETAIL_X_OFFSET = SPLIT_X_OFFSET + 18;
    private static final int DETAIL_WIDTH = 120;


    // ====== constructor, and open static =======
    public static void open(
            Supplier<List<ClueType>> typeSupplier,
            Function<ClueType, List<ClueObject>> clueSupplier
    ) {
        Minecraft.getInstance().setScreen(new WandScreen(
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


        // build widgets

        this.list = new AutoUpdatedScrollableListWidget<ClueObject>(
                this.minecraft,
                (this.width - TEXTURE_WIDTH) / 2 + LIST_X_OFFSET,
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET,
                1,
                LIST_WIDTH, LIST_HEIGHT,
                ENTRY_HEIGHT,
                () -> this.clueSupplier.apply(this.typeSupplier.get().getFirst()),
                (obj) -> obj.getId(),
//                Component::literal,
                (clue) -> Component.literal(clue.getId().toString()),  // TODO
                (clueObject) -> {  /// TODO
                    if (clueObject == null) return;
                    this.details.updateObject(clueObject);
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
                (clueType) -> {   //// TODO: test hot update... or maybe delete the feature
//                    this.typeSupplier = () -> List.of(ClueType.MANUAL, ClueType.MANUAL, ClueType.MANUAL);
                    this.list.updateDataSupplier(
                            () -> clueSupplier.apply(clueType)
                    );
                    // TODO: whether show CREATE button
                }
        );

        Button createButton = Button.builder(Component.literal("+"), btn -> {
            PacketDistributor.sendToServer(new ManualClueCreatePayload());
        }).bounds(
                (this.width - TEXTURE_WIDTH) / 2 + LIST_X_OFFSET + 20,
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET - 16 -10,
                60, 16
        ).build();

        this.details = new DetailPanel(
                Minecraft.getInstance(),
                DETAIL_WIDTH,
                LIST_HEIGHT - 4,
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET,
                (this.width - TEXTURE_WIDTH) / 2 + DETAIL_X_OFFSET
//                List.of(
//                        new ReadOnlyDetailWidget(0, 0, 100, 100,
//                                "title", List.of("111", "222", "33333333333333333333333 333333333333333333333333"))
//                        new ReadOnlyDetailWidget(0, 0, 100, 100,
//                                "title", List.of("111", "222", "33333333333333333333333 333333333333333333333333")),
//                        new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.literal("0.7 scale aaaaaaaaaaaa"), 0.7f),
//                        new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.literal("1.2 scale aaaaaaaaaaaa"), 1.2f),
//                        new SplitLineRow(0, 0, 100, 100, 2, 2),
//                        new TextListWithIndexRow(0, 0, 100, 100, 2, 2,
//                                List.of("sentence 1", "sentence 2: 一句很长长长长长长长长长长长的中文"), 2)
//                        )
        );

        // click event order
        this.addRenderableWidget(this.tab);
        this.addRenderableWidget(this.list);
        this.addRenderableWidget(createButton);
        this.addRenderableWidget(details);

    }


    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);

        // render widgets
        super.render(gfx, mouseX, mouseY, partialTick);

//        // Tooltips (e.g. from item icon)
//        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - TEXTURE_WIDTH) / 2;
        int top = (this.height - TEXTURE_HEIGHT) / 2;

        // book texture
        gfx.blit(BOOK_TEXTURE, left, top, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
