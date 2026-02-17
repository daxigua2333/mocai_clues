package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.component.data.ItemClue;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosWithFace;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;
import io.github.daxigua2333.mocai_clues.guis.whitelist.ItemClueHolder;
import io.github.daxigua2333.mocai_clues.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.DropdownWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.container.DetailPanelNew;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectUpdatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class AttachedClueEditorScreen extends ClueBookScreenLayout implements ItemClueHolder {
    private final Supplier<List<ClueType>> typeSupplier;
    private final Function<ClueType, List<ClueObject>> clueSupplier;
    private final Consumer<ClueObject> applyChange;
    private final Consumer<ClueObject> deleteCurrent;
    private final ISerializableLocation location;
    private final BlockPosWithFace posWithFace;


    private static final int ENTRY_HEIGHT = 20;

    private Button createButton;
    private DropdownWidget<ClueType> tab;
    private AutoUpdatedScrollableListWidget<ClueObject> list;
    private DetailPanelNew details;

    // ====== constructor, and open static =======
    public static void open(
            Supplier<List<ClueType>> typeSupplier,
            Function<ClueType, List<ClueObject>> clueSupplier,
            Consumer<ClueObject> applyChange,
            Consumer<ClueObject> deleteCurrent,
            ISerializableLocation location,
            BlockPosWithFace posWithFace
    ) {
        Minecraft.getInstance().setScreen(new AttachedClueEditorScreen(
                typeSupplier, clueSupplier, applyChange, deleteCurrent, location, posWithFace
        ));
    }

    public AttachedClueEditorScreen(
            Supplier<List<ClueType>> typeSupplier,
            Function<ClueType, List<ClueObject>> clueSupplier,
            Consumer<ClueObject> applyChange,
            Consumer<ClueObject> deleteCurrent,
            ISerializableLocation location,
            BlockPosWithFace posWithFace
    ) {
        super(Component.translatable(MoCaiClues.MODID + "screen.editor"));
        this.typeSupplier = typeSupplier;
        this.clueSupplier = clueSupplier;
        this.applyChange = applyChange;
        this.deleteCurrent = deleteCurrent;
        this.location = location;
        this.posWithFace = posWithFace;
    }

    // ===== build and add widgets =======
    @Override
    protected void init() {
        super.init();

        // build widgets
        int left = (this.width - WHOLE_W) / 2;
        int top = (this.height - WHOLE_H) / 2;

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

        createButton = Button.builder(Component.literal("+"), btn -> {
            // If work as expected, only MANUAL and ITEM can trigger this
            ClueObject defaultObj = Assembler.createDefaultByType(tab.getSelected());
            BlockPosWithFace compo = defaultObj.getComponentOrThrow(ComponentType.BLOCK_POS_WITH_FACE);
            compo.update(posWithFace);

            PacketDistributor.sendToServer(new ClueObjectUpdatePayload(
                    location,
                    new ClueObjectUpdatePayload.Data(defaultObj)));
//            PacketDistributor.sendToServer(new ScreenCreateDefaultCluePayload(tab.getSelected(), location));
        }).bounds(left + LIST_X_OFFSET + 13 + 60 + 4, top + LIST_Y_OFFSET + 11, 16, 16).build();

        tab = new DropdownWidget<>(
                left + LIST_X_OFFSET + 13,
                top + LIST_Y_OFFSET + 11,
                2,
                60,
                16,
                0,
                this.typeSupplier,
//                Component::literal,
                (type) -> Component.literal(type.toString()),  // TODO
                (clueType) -> {
                    list.updateDataSupplier(
                            () -> clueSupplier.apply(clueType)
                    );
                    createButton.visible = clueType == ClueType.MANUAL || clueType == ClueType.ITEM;
                }
        );

        // click event order
        this.addRenderableWidget(tab);
        this.addRenderableWidget(list);
        this.addRenderableWidget(details);
        this.addRenderableWidget(createButton);
    }

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
