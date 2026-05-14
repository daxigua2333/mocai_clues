package io.github.daxigua2333.cmagic_clue.guis;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import io.github.daxigua2333.cmagic_clue.component.data.InfoData;
import io.github.daxigua2333.cmagic_clue.data.ModAttachmentRegistry;
import io.github.daxigua2333.cmagic_clue.data.ObjectHolder;
import io.github.daxigua2333.cmagic_clue.data.location.FromClueBook;
import io.github.daxigua2333.cmagic_clue.data.location.factory.ISerializableLocation;
import io.github.daxigua2333.cmagic_clue.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.cmagic_clue.guis.widget.container.DetailPanelInClueBook;
import io.github.daxigua2333.cmagic_clue.networks.ClueObjectUpdatePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClueBookScreen extends ClueBookScreenLayout {

    private static final int ENTRY_HEIGHT = 20;

    private final ISerializableLocation location;

    public ClueBookScreen(ISerializableLocation location) {
        super(Component.translatable(CMagicClue.MODID + "screen.clue_book"));
        this.location = location;
    }

    public ClueBookScreen(LocalPlayer player) {
        this(new FromClueBook(player).getSerializable());
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - WHOLE_W) / 2;
        int top = (this.height - WHOLE_H) / 2;

        var details = new DetailPanelInClueBook(
                Minecraft.getInstance(),
                PANEL_W - 36,
                PANEL_H - 84,
                top + PANEL_Y_OFFSET + 20,
                left + PANEL_X_OFFSET + 20,
                obj -> PacketDistributor.sendToServer(new ClueObjectUpdatePayload(
                        location,
                        new ClueObjectUpdatePayload.Data(obj.getId())
                ))
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
