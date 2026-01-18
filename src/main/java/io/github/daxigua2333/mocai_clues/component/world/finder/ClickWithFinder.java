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

    @Override
    public ComponentType type() {
        return ComponentType.SEND_CLUE;
    }


//    public static final Codec<ClickWithFinder> CODEC = Codec.unit(Unit.INSTANCE).xmap(
//            u -> new ClickWithFinder(),
//            v -> Unit.INSTANCE  // encode: no data
//    );

    public static final Codec<ClickWithFinder> CODEC = Codec.unit(new ClickWithFinder());

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
