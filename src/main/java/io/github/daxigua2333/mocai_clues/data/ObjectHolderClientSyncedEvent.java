package io.github.daxigua2333.mocai_clues.data;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

/** IAttachmentHolder is null, means it is in SavedData env */
// TODO: more context
public class ObjectHolderClientSyncedEvent extends Event {

    @Nullable
    private final IAttachmentHolder attachmentHolder;



    protected ObjectHolderClientSyncedEvent(@Nullable IAttachmentHolder attachmentHolder) {
        this.attachmentHolder = attachmentHolder;
    }

    public @Nullable IAttachmentHolder getAttachmentHolder() {
        return attachmentHolder;
    }

    public static class Pre extends ObjectHolderClientSyncedEvent {
        public Pre(IAttachmentHolder attachmentHolder) {super(attachmentHolder);}
    }
    public static class Post extends ObjectHolderClientSyncedEvent {
        public Post(IAttachmentHolder attachmentHolder) {super(attachmentHolder);}
    }

}
