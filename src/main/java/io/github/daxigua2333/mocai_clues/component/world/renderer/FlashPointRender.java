package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlashPointRender extends BasePass {

    @Override
    public RenderType renderType() {
        return null;
    }

    @Override
    public void addToMesh(BufferBuilder builder) {

    }

    @Override
    public void setupRenderState() {

    }

    @Override
    public void clearRenderState() {

    }

    @Override
    public ComponentType type() {
        return ComponentType.FLASH_POINT_RENDERER;
    }

    public static final Codec<FlashPointRender> CODEC = Codec.unit(new FlashPointRender());

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
