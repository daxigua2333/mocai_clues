package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.WorldBlockPos;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockOutlineRenderer extends BaseRenderer{
    private static final int ARGB = 0xFF000000;

    @Override
    public ComponentType type() {
        return ComponentType.BLOCK_OUTLINE_RENDERER;
    }

    @Override
    public Pass pass() {
        return Pass.BLOCK_OUTLINE;
    }
    @Override
    public void addToMesh(BufferBuilder builder) {
        // get block pos
        BlockPos pos;
        MoCaiClues.LOGGER.debug("whyyyyyyyyyyyyyyyy: 1");
        if (this.owner.hasComponent(ComponentType.WORLD_BLOCK_POS)) {
            var compo = (WorldBlockPos) this.owner.getComponent(ComponentType.WORLD_BLOCK_POS);
            pos = compo.getBlockPos();
        MoCaiClues.LOGGER.debug("whyyyyyyyyyyyyyyyy: 2 {}", pos);
        } else {
            throw new RuntimeException("ClueObject should contain WorldBlockPos");
        }

        MoCaiClues.LOGGER.debug("whyyyyyyyyyyyyyyyy: 3");
        // Create a box slightly larger than the block to avoid z-fighting with block faces
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
        MoCaiClues.LOGGER.debug("whyyyyyyyyyyyyyyyy: 4");
        line(builder, minX, minY, minZ, maxX, minY, minZ, ARGB);
        line(builder, maxX, minY, minZ, maxX, minY, maxZ, ARGB);
        line(builder, maxX, minY, maxZ, minX, minY, maxZ, ARGB);
        line(builder, minX, minY, maxZ, minX, minY, minZ, ARGB);
        // Top square
        MoCaiClues.LOGGER.debug("whyyyyyyyyyyyyyyyy: 5");
        line(builder, minX, maxY, minZ, maxX, maxY, minZ, ARGB);
        line(builder, maxX, maxY, minZ, maxX, maxY, maxZ, ARGB);
        line(builder, maxX, maxY, maxZ, minX, maxY, maxZ, ARGB);
        line(builder, minX, maxY, maxZ, minX, maxY, minZ, ARGB);
        // Vertical pillars
        MoCaiClues.LOGGER.debug("whyyyyyyyyyyyyyyyy: 6");
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
//        builder.addVertex(x1, y1, z1).setColor(argb);
//        builder.addVertex(x2, y2, z2).setColor(argb);
    }


    public static Codec<BlockOutlineRenderer> CODEC = Codec.unit(BlockOutlineRenderer::new);

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
