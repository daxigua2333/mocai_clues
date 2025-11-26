package io.github.daxigua2333.mocai_clues.footprints;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.data_attachments.ModDataAttachmentRegistry;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFootprintRegistry {

    public static final RenderType FOOTPRINT_RENDER_TYPE = RenderType.create(
            "footprint_render_type",
                DefaultVertexFormat.POSITION_COLOR,  // smaller vertex format, no UVs, no lightmap :contentReference[oaicite:1]{index=1}
                VertexFormat.Mode.QUADS,
                RenderType.SMALL_BUFFER_SIZE,  // plenty for footprints :contentReference[oaicite:2]{index=2}
                false, // affectsCrumbling – no
                false, // sortOnUpload – we don’t care about strict translucency order
                RenderType.CompositeState.builder()
                        // Simple position+color shader (no UV, no normals) :contentReference[oaicite:0]{index=0}
                        .setShaderState(RenderType.POSITION_COLOR_SHADER)
                        // No texture sampling at all
                        .setTextureState(RenderType.NO_TEXTURE)
                        // Basic depth test so footprints don’t show through walls
                        .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                        // Don’t use lightmap or overlay – cheaper
                        .setLightmapState(RenderType.NO_LIGHTMAP)
                        .setOverlayState(RenderType.NO_OVERLAY)
                        // Don’t mess with special layering
                        .setLayeringState(RenderType.NO_LAYERING)
                        // We want translucency so alpha on the footprint actually works
                        .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                        // Don’t write to depth, only color – lets the ground control depth
                        .setWriteMaskState(RenderType.COLOR_WRITE)
                        // Safer for quads that might flip; you *can* change to CULL for a tiny win
                        .setCullState(RenderType.NO_CULL)
                        .createCompositeState(false)
    );

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MoCaiClues.MODID);
    public static final Supplier<AttachmentType<FootprintBlockPosMap>> FOOTPRINT_MAP = ATTACHMENTS_TYPES.register(
            "footprint_map",
            () -> AttachmentType.builder( () -> new FootprintBlockPosMap())
                    .serialize(FootprintBlockPosMap.CODEC)
                    .sync(FootprintBlockPosMap.STREAM_CODEC)
                    .build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENTS_TYPES.register(bus);
    }
}
