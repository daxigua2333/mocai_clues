package io.github.daxigua2333.mocai_clues.component.world.renderer.pass;

import com.mojang.blaze3d.vertex.BufferBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BasePass {
    // statics
    public abstract PassType getPassType();
    public abstract RenderType getRenderType();
    public abstract void setupRenderState();
    public abstract void clearRenderState();
    public abstract boolean doRender(Minecraft mc);

//    public abstract void addToMesh(BufferBuilder builder, ChunkPos chunkPos);
    public record Context(
            BufferBuilder builder,
            ClueObject obj
    ) {}
    public abstract void addToMesh(Context context);
}
