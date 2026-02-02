package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SendClue extends ClueComponent {

    @Override
    public ComponentType type() {
        return ComponentType.SEND_CLUE;
    }


//    public static final Codec<ClickWithFinder> CODEC = Codec.unit(Unit.INSTANCE).xmap(
//            u -> new ClickWithFinder(),
//            v -> Unit.INSTANCE  // encode: no data
//    );

    public static final Codec<SendClue> CODEC = Codec.unit(new SendClue());

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of();
    }
}
