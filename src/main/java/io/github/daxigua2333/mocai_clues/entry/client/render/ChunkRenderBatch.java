package io.github.daxigua2333.mocai_clues.entry.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BasePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.ModRenderPassRegistry;
import io.github.daxigua2333.mocai_clues.data.client.api.ClientAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@OnlyIn(value = Dist.CLIENT)
public class ChunkRenderBatch {
    // One buffer per RenderType you support
    private final Map<ComponentType, VertexBuffer> buffers = new HashMap<>();
    private boolean dirty = true;
    private boolean isEmpty = true;

    public void setDirty() { this.dirty = true; }
    public boolean isDirty() { return dirty; }
    public boolean isEmpty() { return isEmpty; }

    public void upload(Map<ComponentType, MeshData> meshDataMap) {
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

    public void render(ComponentType type, PoseStack poseStack, Matrix4f projectionMatrix) {
        VertexBuffer vbo = buffers.get(type);
        if (vbo == null || vbo.isInvalid()) {return;}

        BasePass passCompo = ModRenderPassRegistry.getPass(type);
        if (passCompo == null) {
            MoCaiClues.LOGGER.error("ComponentType: '{}' should not appear in render batch.", type, new NullPointerException("Undefined Pass component type."));
            return;
        }
        if (! passCompo.doRender(Minecraft.getInstance())) return;
        RenderType rType = ModRenderPassRegistry.getRenderType(type);
        if (rType == null) throw new RuntimeException("Unregistered render type for pass component type: " + type);

        rType.setupRenderState();
        passCompo.setupRenderState();

        vbo.bind();
        vbo.drawWithShader(poseStack.last().pose(), projectionMatrix, RenderSystem.getShader());
        VertexBuffer.unbind();

        passCompo.clearRenderState();
        rType.clearRenderState();
    }

    public void renderAllTypes(PoseStack poseStack, Matrix4f projectionMatrix) {
        for (ComponentType t : buffers.keySet()) {
            render(t, poseStack, projectionMatrix);
        }
    }

    public void close() {
        buffers.values().forEach(VertexBuffer::close);
        buffers.clear();
        dirty = true;
    }
}
