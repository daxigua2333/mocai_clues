package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class ClickWithFinder extends ClueComponent {
    // TODO: merge, send message callback
    public void send(ServerPlayer player, Function<ClueObject, ClueObject> generateCopy) {
//        if (player.getMainHandItem().getItem() != ModItemsRegistry.CLUE_FINDER_ITEM.get()) return;

        ClueObject object = this.owner;

        // accessible
        FinderState bCompo = object.getComponent(ComponentType.FINDER_STATE);
        if (bCompo == null) throw new RuntimeException("ClueObject#" + this.owner.getId() + " has no FinderState component");
        if (!bCompo.isAccessible(player.getScoreboardName())) return;

        ClueObject copy = generateCopy.apply(object);
        ObjectHolder<ClueObject> holder = player.getData(ModAttachmentRegistry.CLUE_BOOK);
        holder.put(copy);
        player.syncData(ModAttachmentRegistry.CLUE_BOOK);

        bCompo.onFound();
    }

    @Override
    public ComponentType type() {
        return ComponentType.SEND_CLUE;
    }

    public static final Codec<ClickWithFinder> CODEC = Codec.unit(Unit.INSTANCE).xmap(
            u -> new ClickWithFinder(),
            v -> Unit.INSTANCE  // encode: no data
    );

//    public static final Codec<ClickWithFinder> CODEC = Codec.unit(new ClickWithFinder());

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
