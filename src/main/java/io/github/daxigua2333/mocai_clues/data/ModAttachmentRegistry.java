package io.github.daxigua2333.mocai_clues.data;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.common.IndexManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Function;
import java.util.function.Supplier;

public final class ModAttachmentRegistry {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MoCaiClues.MODID);

    // add an extra step that ensure index
    private static final Codec<ObjectHolder<ClueObject>> CLUE_HOLDER_CODEC =
            ObjectHolder.codec(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC).xmap(
                    holder -> {
                        IndexManager.Server.attachmentHolderEnsure(holder);
                        return holder;
                    }, // decode
                    Function.identity()
            );


    private static ObjectHolder<ClueObject> createDefault() {
        var holder = new ObjectHolder<>(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC);
        IndexManager.Server.attachmentHolderEnsure(holder);
        return holder;
    }

    public static final Supplier<AttachmentType<ObjectHolder<ClueObject>>> CLUE_OBJECT_HOLDER = ATTACHMENTS_TYPES.register(
            "clue_object_holder",
            () -> AttachmentType.builder(ModAttachmentRegistry::createDefault)
                    .serialize(CLUE_HOLDER_CODEC)
//                    .sync(FootprintMainMap.STREAM_CODEC)
                    .sync(new ObjectHolderSyncHandler<>(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC))
                    .build()
    );
    public static final Supplier<AttachmentType<ObjectHolder<ClueObject>>> CLUE_BOOK = ATTACHMENTS_TYPES.register(
            "clue_book",
            () -> AttachmentType.builder(ModAttachmentRegistry::createDefault)
                    .serialize(CLUE_HOLDER_CODEC)
//                    .sync(FootprintMainMap.STREAM_CODEC)
                    .sync(new ObjectHolderSyncHandler<>(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC))
                    .build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENTS_TYPES.register(bus);
    }

}
