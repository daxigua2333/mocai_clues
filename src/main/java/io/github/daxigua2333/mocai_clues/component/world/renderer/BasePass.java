package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;


public abstract class BasePass extends ClueComponent {
    // statics
    public abstract RenderType renderType();
    public abstract void setupRenderState();
    public abstract void clearRenderState();
    public abstract boolean doRender(Minecraft mc);

//    public abstract void addToMesh(BufferBuilder builder, ChunkPos chunkPos);
    public abstract void addToMesh(BufferBuilder builder);
}
