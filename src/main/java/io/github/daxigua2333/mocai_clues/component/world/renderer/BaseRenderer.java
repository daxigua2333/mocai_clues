package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.entry.client.render.ModRenderType;
import net.minecraft.client.renderer.RenderType;


public abstract class BaseRenderer extends ClueComponent {
    public enum Pass {
        BLOCK_OUTLINE(ModRenderType.OVERLAY_LINES),
        ;
        public final RenderType renderType;
        Pass(RenderType renderType) {
            this.renderType = renderType;
        }
//        public VertexFormat.Mode mode() {
//            return renderType.mode();
//        }
//        public VertexFormat format() {
//            return renderType.format();
//        }
//        public BufferBuilder bufferBuilder(ByteBufferBuilder byteBuffer) {
//            return new BufferBuilder(byteBuffer, mode(), format());
//        }
    }

    public abstract Pass pass();
//    public abstract void addToMesh(BufferBuilder builder, ChunkPos chunkPos);
    public abstract void addToMesh(BufferBuilder builder);
}
