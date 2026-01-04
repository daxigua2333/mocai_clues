package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.data.BaseRendererData;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BasePass;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RendererHolder extends ClueComponent {
    private final List<BaseRendererData> backing;

    public RendererHolder(List<BaseRendererData> backing) {
        this.backing = backing;
    }
    private RendererHolder() {
        this(new ArrayList<>());
    }
    private List<BaseRendererData> getBacking() {
        return backing;
    }

    public List<BaseRendererData> getImmutable() {
        return Collections.unmodifiableList(backing);
    }


    @Override
    public ComponentType type() {
        return ComponentType.RENDERER_HOLDER;
    }

    public static final Codec<RendererHolder> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BaseRendererData.CODEC.listOf().fieldOf("backing").forGetter(RendererHolder::getBacking)
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
