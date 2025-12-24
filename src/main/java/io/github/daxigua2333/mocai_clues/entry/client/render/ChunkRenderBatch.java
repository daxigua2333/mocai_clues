package io.github.daxigua2333.mocai_clues.entry.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ChunkRenderBatch {
    // One buffer per RenderType you support
    private final Map<RenderType, VertexBuffer> buffers = new HashMap<>();
    private boolean dirty = true;
    private boolean isEmpty = true;

    public void setDirty() { this.dirty = true; }
    public boolean isDirty() { return dirty; }
    public boolean isEmpty() { return isEmpty; }

    public void upload(Map<RenderType, MeshData> meshDataMap) {
        meshDataMap.forEach((type, data) -> {
//            VertexBuffer vbo = buffers.computeIfAbsent(type, t -> new VertexBuffer(VertexBuffer.Usage.STATIC));
            VertexBuffer vbo = buffers.computeIfAbsent(type, t -> new VertexBuffer(VertexBuffer.Usage.DYNAMIC));
//            VertexBuffer vbo = buffers.get(type);
//            if (vbo != null) vbo.close();
//            vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
            MoCaiClues.LOGGER.debug("======uploading: {} {}", data.drawState().vertexCount(), data.drawState().mode());
            MoCaiClues.LOGGER.debug("======uploading: {}: {}", buffers.size(), buffers);
            vbo.bind();
            vbo.upload(data);
            VertexBuffer.unbind();
        });
        this.isEmpty = meshDataMap.isEmpty();
        this.dirty = false;
    }

    public void render(RenderType type, PoseStack poseStack, Matrix4f projectionMatrix) {
        VertexBuffer vbo = buffers.get(type);
        if (vbo == null || vbo.isInvalid()) {return;}
        type.setupRenderState();
//var shader = GameRenderer.getRendertypeLinesShader();
//Supplier<ShaderInstance> sup = () -> GameRenderer.getRendertypeLinesShader();
//RenderSystem.setShader(sup);
//MoCaiClues.LOGGER.debug("{}\n{}", RenderSystem.getShader(), GameRenderer.getRendertypeLinesShader());
RenderSystem.disableDepthTest();  // TODO
//RenderSystem.depthMask(false);
//RenderSystem.enableBlend();
//RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f,1f,1f,1f);
        vbo.bind();
        vbo.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
//        vbo.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
//        vbo.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();
        type.clearRenderState();
//RenderSystem.depthMask(true);
RenderSystem.enableDepthTest();
//RenderSystem.disableBlend();
    }

    public void renderAllTypes(PoseStack poseStack, Matrix4f projectionMatrix) {
        for (RenderType t : buffers.keySet()) {
            render(t, poseStack, projectionMatrix);
        }
    }

    public void close() {
        buffers.values().forEach(VertexBuffer::close);
        buffers.clear();
        dirty = true;
    }
}
