package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalDouble;

public class BlockOutlinePass extends BasePass {
    private static final int ARGB = 0xFF000000;

    @Override
    public ComponentType type() {
        return ComponentType.BLOCK_OUTLINE_RENDERER;
    }

    @Override
    public RenderType renderType() {
        return RenderType.create(
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

    @Override
    public void setupRenderState() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1f,1f,1f,1f);
    }

    @Override
    public void clearRenderState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }


    @Override
    public void addToMesh(BufferBuilder builder) {
        BlockPosList compo = this.owner.getComponent(ComponentType.BLOCK_POS_LIST);
        if (compo != null) {
            for (var pos : compo.getImmutable()) {
                box(builder, pos);
            }
        } else {
//            throw new RuntimeException("ClueObject should contain WorldBlockPos");
        }
    }

    // Create a box slightly larger than the block to avoid z-fighting with block faces
    private static void box(BufferBuilder builder, BlockPos pos) {
//        LevelRenderer.renderLineBox(builder, pos.getX(), pos.getY(), pos.getZ(), pos.getX()+1, pos.getY()+1, pos.getZ()+1,
//                1f, 1f, 0f, 1f);
        AABB box = new AABB(pos).inflate(0.002);

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;
        // Draw the 12 edges of the cube
        // Bottom square
        line(builder, minX, minY, minZ, maxX, minY, minZ, ARGB);
        line(builder, maxX, minY, minZ, maxX, minY, maxZ, ARGB);
        line(builder, maxX, minY, maxZ, minX, minY, maxZ, ARGB);
        line(builder, minX, minY, maxZ, minX, minY, minZ, ARGB);
        // Top square
        line(builder, minX, maxY, minZ, maxX, maxY, minZ, ARGB);
        line(builder, maxX, maxY, minZ, maxX, maxY, maxZ, ARGB);
        line(builder, maxX, maxY, maxZ, minX, maxY, maxZ, ARGB);
        line(builder, minX, maxY, maxZ, minX, maxY, minZ, ARGB);
        // Vertical pillars
        line(builder, minX, minY, minZ, minX, maxY, minZ, ARGB);
        line(builder, maxX, minY, minZ, maxX, maxY, minZ, ARGB);
        line(builder, maxX, minY, maxZ, maxX, maxY, maxZ, ARGB);
        line(builder, minX, minY, maxZ, minX, maxY, maxZ, ARGB);
    }

    private static void line(BufferBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2, int argb) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (len != 0.0f) {
            dx /= len;
            dy /= len;
            dz /= len;
        }
        builder.addVertex(x1, y1, z1).setColor(argb).setNormal(dx, dy, dz);
        builder.addVertex(x2, y2, z2).setColor(argb).setNormal(dx, dy, dz);
    }


    public static Codec<BlockOutlinePass> CODEC = Codec.unit(BlockOutlinePass::new);

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
