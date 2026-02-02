package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.container.DetailPanelNew;
import io.github.daxigua2333.mocai_clues.guis.widget.DropdownWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AttachedClueEditorScreen extends ClueBookScreenLayout {
    private final Supplier<List<ClueType>> typeSupplier;
    private final Function<ClueType, List<ClueObject>> clueSupplier;
    private final Consumer<ClueObject> applyChange;
    private final Consumer<ClueObject> deleteCurrent;


    private static final int ENTRY_HEIGHT = 20;


    // ====== constructor, and open static =======
    public static void open(
            Supplier<List<ClueType>> typeSupplier,
            Function<ClueType, List<ClueObject>> clueSupplier,
            Consumer<ClueObject> applyChange,
            Consumer<ClueObject> deleteCurrent
    ) {
        Minecraft.getInstance().setScreen(new AttachedClueEditorScreen(
                typeSupplier, clueSupplier, applyChange, deleteCurrent
        ));
    }

    public AttachedClueEditorScreen(
            Supplier<List<ClueType>> typeSupplier,
            Function<ClueType, List<ClueObject>> clueSupplier,
            Consumer<ClueObject> applyChange,
            Consumer<ClueObject> deleteCurrent
    ) {
        super(Component.translatable(MoCaiClues.MODID + "screen.editor"));
        this.typeSupplier = typeSupplier;
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

        var details = new DetailPanelNew(
                Minecraft.getInstance(),
                PANEL_W - 36,
                PANEL_H - 64,
                top + PANEL_Y_OFFSET + 40,
                left + PANEL_X_OFFSET + 20,
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
                () -> {
                    List<ClueType> types = this.typeSupplier.get();
                    return this.clueSupplier.apply(types.isEmpty() ? null : types.getFirst());
                },
                (obj) -> obj.getId(),
//                Component::literal,
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

        var tab = new DropdownWidget<>(   // TODO: test sync
                left + LIST_X_OFFSET + 13,
                top + LIST_Y_OFFSET + 11,
                2,
                60,
                16,
                0,
                this.typeSupplier,
//                Component::literal,
                (type) -> Component.literal(type.toString()),  // TODO
                (clueType) -> {   //// TODO: test hot update... or maybe delete the feature
                    list.updateDataSupplier(
                            () -> clueSupplier.apply(clueType)
                    );
                }
        );

        // click event order
        this.addRenderableWidget(tab);
        this.addRenderableWidget(list);
        this.addRenderableWidget(details);

    }

}
