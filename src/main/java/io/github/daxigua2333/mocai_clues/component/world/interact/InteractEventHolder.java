package io.github.daxigua2333.mocai_clues.component.world.interact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InteractEventHolder extends ClueComponent {

    private final List<InteractEvent> backing;

    public InteractEventHolder(List<InteractEvent> backing) {
        this.backing = backing;
    }
    private InteractEventHolder() {
        this(new ArrayList<>());
    }
    private List<InteractEvent> getBacking() {
        return backing;
    }


    public List<InteractEvent> getImmutable() {
        return Collections.unmodifiableList(backing);
    }


    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_EVENT_HOLDER;
    }

    public static final Codec<InteractEventHolder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            InteractEvent.CODEC.listOf().fieldOf("backing").forGetter(InteractEventHolder::getBacking)
    ).apply(inst, InteractEventHolder::new));

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
