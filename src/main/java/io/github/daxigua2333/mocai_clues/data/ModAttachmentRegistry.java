package io.github.daxigua2333.mocai_clues.data;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintAttachedPosIndexMap;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintMainMap;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintSyncHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachmentRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MoCaiClues.MODID);
    public static final Supplier<AttachmentType<ObjectHolder<ClueObject>>> CLUE_OBJECT_HOLDER = ATTACHMENTS_TYPES.register(
            "clue_object_holder",
            () -> AttachmentType.builder( () -> new ObjectHolder<>(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC))
                    .serialize(ObjectHolder.codec(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC))
//                    .sync(FootprintMainMap.STREAM_CODEC)
                    .sync(new ObjectHolderSyncHandler<>(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC))
                    .build()
    );
    public static final Supplier<AttachmentType<ObjectHolder<ClueObject>>> CASE_BOOK = ATTACHMENTS_TYPES.register(
            "case_book",
            () -> AttachmentType.builder( () -> new ObjectHolder<>(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC))
                    .serialize(ObjectHolder.codec(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC))
//                    .sync(FootprintMainMap.STREAM_CODEC)
                    .sync(new ObjectHolderSyncHandler<>(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC))
                    .build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENTS_TYPES.register(bus);
    }

}
