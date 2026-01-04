package io.github.daxigua2333.mocai_clues.component.world.renderer.pass;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class BasePass {
    // statics
    @OnlyIn(Dist.CLIENT) public abstract PassType getPassType();
    @OnlyIn(Dist.CLIENT) public abstract RenderType getRenderType();
    @OnlyIn(Dist.CLIENT) public abstract void setupRenderState();
    @OnlyIn(Dist.CLIENT) public abstract void clearRenderState();
//    @OnlyIn(Dist.CLIENT) public abstract ShaderInstance getShader();
    @OnlyIn(Dist.CLIENT) public abstract boolean doRender(Minecraft mc);

    @OnlyIn(Dist.CLIENT) public abstract void addToMesh(BufferBuilder builder, ChunkPos chunkPos);
//    public record Context(
//            BufferBuilder builder,
//            ClueObject obj
//    ) {}
//    public abstract void addToMesh(Context context);

    public static final Codec<BasePass> CODEC = PassType.CODEC.dispatch(
            "type",
            BasePass::getPassType,
            PassType::codec
    );
}
