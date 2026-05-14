package io.github.daxigua2333.cmagic_clue.component.system.renderer.pass;

import com.mojang.blaze3d.vertex.BufferBuilder;
import io.github.daxigua2333.cmagic_clue.component.system.renderer.PassType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class BasePass {
    // statics
    public abstract PassType getPassType();
    public abstract RenderType getRenderType();
    public abstract void setupRenderState();
    public abstract void clearRenderState();
//    @OnlyIn(Dist.CLIENT) public abstract ShaderInstance getShader();
    public abstract boolean doRender(Minecraft mc);

    public abstract void addToMesh(BufferBuilder builder, ChunkPos chunkPos);
//    public record Context(
//            BufferBuilder builder,
//            ClueObject obj
//    ) {}
//    public abstract void addToMesh(Context context);

}
