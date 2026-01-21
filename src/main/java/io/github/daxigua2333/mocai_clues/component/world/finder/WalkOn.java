package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WalkOn extends ClueComponent {
    @Override
    public ComponentType type() {
        return ComponentType.WALK_ON;
    }

    public static final Codec<WalkOn> CODEC = Codec.unit(new WalkOn());

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
