//package io.github.daxigua2333.mocai_clues.guis;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import io.github.daxigua2333.mocai_clues.component.ClueType;
//import io.github.daxigua2333.mocai_clues.guis.widget.AutoUpdatedScrollableListWidget;
//import io.github.daxigua2333.mocai_clues.guis.widget.DetailPanelNew;
//import io.github.daxigua2333.mocai_clues.guis.widget.DropdownWidget;
//import io.github.daxigua2333.mocai_clues.networks.ManualClueCreatePayload;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.network.chat.Component;
//import net.neoforged.neoforge.network.PacketDistributor;
//
//import java.util.List;
//import java.util.function.Function;
//import java.util.function.Supplier;
//
//@Deprecated
//public class WandScreen extends ClueBookScreenLayout {
//    private Supplier<List<ClueType>> typeSupplier;
//    private Function<ClueType, List<ClueObject>> clueSupplier;
//
//    private DropdownWidget<ClueType> tab;
//    private AutoUpdatedScrollableListWidget<ClueObject> list;
//    private DetailPanelNew details;
//
//    private static final int ENTRY_HEIGHT = 20;
//
//
//    // ====== constructor, and open static =======
//    public static void open(
//            Supplier<List<ClueType>> typeSupplier,
//            Function<ClueType, List<ClueObject>> clueSupplier
//    ) {
//        Minecraft.getInstance().setScreen(new WandScreen(
//                typeSupplier,
//                clueSupplier
//        ));
//    }
//
//    public WandScreen(
//            Supplier<List<ClueType>> typeSupplier,
//            Function<ClueType, List<ClueObject>> clueSupplier
//    ) {
//        super(Component.translatable("screen." + MoCaiClues.MODID + ".data_selection"));
//        this.typeSupplier = typeSupplier;
//        this.clueSupplier = clueSupplier;
//    }
//
//    // ===== build and add widgets =======
//    @Override
//    protected void init() {
//        super.init();
//
//        this.clearWidgets();
//
//
//        // build widgets
//        int left = (this.width - WHOLE_W) / 2;
//        int top = (this.height - WHOLE_H) / 2;
//
//        this.list = new AutoUpdatedScrollableListWidget<>(
//                this.minecraft,
//                left + LIST_X_OFFSET,
//                top + LIST_Y_OFFSET,
//                1,
//                LIST_W, LIST_H,
//                ENTRY_HEIGHT,
//                () -> {
//                    List<ClueType> types = this.typeSupplier.get();
//                    return this.clueSupplier.apply(types.isEmpty() ? null : types.getFirst());
//                },
//                (obj) -> obj.getId(),
////                Component::literal,
//                (clue) -> Component.literal(clue.getId().toString()),  // TODO
//                (clueObject) -> {
//                    if (clueObject == null) return;
//                    this.details.updateObject(clueObject);
//                }
//        );
//
//        this.tab = new DropdownWidget<>(   // TODO: test sync
//                left + LIST_X_OFFSET - 2,
//                top + LIST_Y_OFFSET - 16,
//                2,
//                60,
//                16,
//                0,
//                this.typeSupplier,
////                Component::literal,
//                (type) -> Component.literal(type.toString()),  // TODO
//                (clueType) -> {   //// TODO: test hot update... or maybe delete the feature
////                    this.typeSupplier = () -> List.of(ClueType.MANUAL, ClueType.MANUAL, ClueType.MANUAL);
//                    this.list.updateDataSupplier(
//                            () -> clueSupplier.apply(clueType)
//                    );
//                    // TODO: whether show CREATE button
//                }
//        );
//
//        Button createButton = Button.builder(Component.literal("+"), btn -> {
//            PacketDistributor.sendToServer(new ManualClueCreatePayload());
//        }).bounds(
////                (this.width - TEXTURE_WIDTH) / 2 + LIST_X_OFFSET + 20,
//                left + LIST_X_OFFSET - 2 + 60,
////                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET - 16 -10,
//                top + LIST_Y_OFFSET - 16,
//                20, 16
//        ).build();
//
//        this.details = new DetailPanelNew(
//                Minecraft.getInstance(),
//                PANEL_W,
//                PANEL_H,
//                top + PANEL_Y_OFFSET,
//                left + PANEL_X_OFFSET
//        );
//
//        // click event order
//        this.addRenderableWidget(this.tab);
//        this.addRenderableWidget(this.list);
//        this.addRenderableWidget(createButton);
//        this.addRenderableWidget(details);
//
//    }
//
//}
