package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SendClue extends ClueComponent {

    public void send(Player player) {
        if (player.getMainHandItem().getItem() != ModItemsRegistry.CLUE_FINDER_ITEM.get()) return;

        ClueObject object = this.owner;

        FinderState bCompo = object.getComponent(ComponentType.FINDER_STATE);
        if (bCompo == null) throw new RuntimeException("ClueObject#" + this.owner.getId() + " has no FinderState component");
        if (!bCompo.isAccessible(player.getScoreboardName())) return;

        DetailData compo = object.getComponent(ComponentType.DETAIL_DATA);
        if (compo == null) return;

        bCompo.onFound();

        player.sendSystemMessage(Component.literal(compo.getName()));
        compo.getDetails().forEach(s -> {
            player.sendSystemMessage(Component.literal(s));
        });
    }

    @Override
    public ComponentType type() {
        return ComponentType.SEND_CLUE;
    }

    public static final Codec<SendClue> CODEC = Codec.unit(new SendClue());

    @Nullable
    @Override
    public List<AbstractWidget> getEditable() {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of();
    }
}
