package io.github.daxigua2333.mocai_clues.entry.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BasePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(value = Dist.CLIENT)
public class ChunkRenderBatch {
    // One buffer per RenderType you support
    private final Map<PassType, VertexBuffer> buffers = new HashMap<>();
    private boolean dirty = true;
    private boolean isEmpty = true;

    public void setDirty() { this.dirty = true; }
    public boolean isDirty() { return dirty; }
    public boolean isEmpty() { return isEmpty; }

    public void upload(Map<PassType, MeshData> meshDataMap) {
        meshDataMap.forEach((type, data) -> {
//            VertexBuffer vbo = buffers.computeIfAbsent(type, t -> new VertexBuffer(VertexBuffer.Usage.STATIC));
            VertexBuffer vbo = buffers.computeIfAbsent(type, t -> new VertexBuffer(VertexBuffer.Usage.DYNAMIC));
            vbo.bind();
            vbo.upload(data);
            VertexBuffer.unbind();
        });
        this.isEmpty = meshDataMap.isEmpty();
        this.dirty = false;
    }

    public void render(PassType type, PoseStack poseStack, Matrix4f projectionMatrix) {
        VertexBuffer vbo = buffers.get(type);
        if (vbo == null || vbo.isInvalid()) {return;}

        BasePass pass = PassType.getPass(type);
        if (pass == null) {
            MoCaiClues.LOGGER.error("PassType: '{}' should not appear in render batch.", type, new NullPointerException("Undefined Pass component type."));
            return;
        }
        if (! pass.doRender(Minecraft.getInstance())) return;
        RenderType rType = pass.getRenderType();
        if (rType == null) throw new RuntimeException("Unregistered render type for pass component type: " + type);

        rType.setupRenderState();
        pass.setupRenderState();

        vbo.bind();
        vbo.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();

        pass.clearRenderState();
        rType.clearRenderState();
    }

    public void renderAllTypes(PoseStack poseStack, Matrix4f projectionMatrix) {
        for (PassType t : buffers.keySet()) {
            render(t, poseStack, projectionMatrix);
        }
    }

    public void close() {
        buffers.values().forEach(VertexBuffer::close);
        buffers.clear();
        dirty = true;
    }
}
