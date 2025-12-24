package io.github.daxigua2333.mocai_clues.entry.client.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import java.util.OptionalDouble;

public class ModRenderType {


    public static final RenderType OVERLAY_LINES = RenderType.create(
            MoCaiClues.MODID +":overlay_lines",
//            DefaultVertexFormat.POSITION_COLOR,
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
//            1536, // Buffer size
            256,
            false, // useDelegate
            false, // isAlbum
            RenderType.CompositeState.builder()
//                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
//                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLinesShader))
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0D))) // Line thickness
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING) // Prevents Z-fighting
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST) // THIS makes it X-Ray
//                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );
}
