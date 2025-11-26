package io.github.daxigua2333.mocai_clues.data_attachments;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModDataAttachmentRegistry {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MoCaiClues.MODID);
    public static final Supplier<AttachmentType<ClueContainerMap>> CLUE_CONTAINER_MAP =
        ATTACHMENTS_TYPES.register("clue_container_map", () ->
            AttachmentType.serializable(ClueContainerMap::new).build()  // TODO:
        );

    public static void register(IEventBus bus) {
        ATTACHMENTS_TYPES.register(bus);
    }

}
