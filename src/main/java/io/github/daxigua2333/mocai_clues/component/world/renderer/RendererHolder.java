package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RendererHolder extends ClueComponent {
    private final List<PassType> backing;

    public RendererHolder(List<PassType> backing) {
        this.backing = backing;
    }
    private RendererHolder() {
        this(new ArrayList<>());
    }
    private List<PassType> getBacking() {
        return backing;
    }

    public List<PassType> getImmutable() {
        return Collections.unmodifiableList(backing);
    }


    @Override
    public ComponentType type() {
        return ComponentType.RENDERER_HOLDER;
    }

    public static final Codec<RendererHolder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            PassType.CODEC.listOf().fieldOf("backing").forGetter(RendererHolder::getBacking)
    ).apply(inst, RendererHolder::new));

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
