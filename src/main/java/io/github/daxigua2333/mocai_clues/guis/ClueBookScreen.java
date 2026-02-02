package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.container.DetailPanelInClueBook;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClueBookScreen extends ClueBookScreenLayout {

    private static final int ENTRY_HEIGHT = 20;

    public ClueBookScreen() {
        super(Component.translatable(MoCaiClues.MODID + "screen.clue_book"));
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - WHOLE_W) / 2;
        int top = (this.height - WHOLE_H) / 2;

        var details = new DetailPanelInClueBook(
                Minecraft.getInstance(),
                PANEL_W - 36,
                PANEL_H - 64,
                top + PANEL_Y_OFFSET + 40,
                left + PANEL_X_OFFSET + 20
        );

        var list = new AutoUpdatedScrollableListWidget<>(
                this.minecraft,
                left + LIST_X_OFFSET + 8,
                top + LIST_Y_OFFSET + 14,
                1,
                LIST_W - 20, LIST_H - 45,
                ENTRY_HEIGHT,
                () -> getHolder() == null ? List.of() : new ArrayList<>(getHolder().values()),
                (obj) -> obj.getId(),
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

        addRenderableWidget(list);
        addRenderableWidget(details);

    }

    @Nullable
    private ObjectHolder<ClueObject> getHolder() {
        Player p = Minecraft.getInstance().player;
        if (p == null) return null;
        return p.getData(ModAttachmentRegistry.CLUE_BOOK);
    }

}
