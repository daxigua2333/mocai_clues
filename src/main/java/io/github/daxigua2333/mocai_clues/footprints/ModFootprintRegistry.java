package io.github.daxigua2333.mocai_clues.footprints;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintAttachedPosIndexMap;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintMainMap;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintSyncHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFootprintRegistry {


    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MoCaiClues.MODID);
    public static final Supplier<AttachmentType<FootprintMainMap>> FOOTPRINT_MAIN_MAP = ATTACHMENTS_TYPES.register(
            "footprint_main_map",
            () -> AttachmentType.builder( () -> new FootprintMainMap())
                    .serialize(FootprintMainMap.CODEC)
//                    .sync(FootprintMainMap.STREAM_CODEC)
                    .sync(new FootprintSyncHandler())
                    .build()
    );
    public static final Supplier<AttachmentType<FootprintAttachedPosIndexMap>> FOOTPRINT_ATTACHED_POS_INDEX_MAP = ATTACHMENTS_TYPES.register(
            "footprint_attached_pos_index_map",
            () -> AttachmentType.builder( () -> new FootprintAttachedPosIndexMap())
                    .serialize(FootprintAttachedPosIndexMap.CODEC)
                    .build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENTS_TYPES.register(bus);
    }
}
