package io.github.daxigua2333.cmagic_clue.data;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class ObjectHolderClientSyncedEvent extends Event {

    /** null if it is in SavedData env */
    @Nullable
    public final IAttachmentHolder attachmentHolder;

    public ObjectHolderClientSyncedEvent(@Nullable IAttachmentHolder attachmentHolder) {
        this.attachmentHolder = attachmentHolder;
    }

    public static class Full<T> extends ObjectHolderClientSyncedEvent {
        public final ObjectHolder<T> full;

        public Full(@Nullable IAttachmentHolder attachmentHolder, ObjectHolder<T> full) {
            super(attachmentHolder);
            this.full = full;
        }
    }

    public static class Delta<T> extends ObjectHolderClientSyncedEvent {
        public final ObjectHolder<T> prev;
        public final ObjectHolder.DeltaPayload<T> delta;

        public Delta(@Nullable IAttachmentHolder attachmentHolder, ObjectHolder<T> prev, ObjectHolder.DeltaPayload<T> delta) {
            super(attachmentHolder);
            this.prev = prev;
            this.delta = delta;
        }
    }

//    public static class Pre extends ObjectHolderClientSyncedEvent {
//        public Pre(IAttachmentHolder attachmentHolder) {super(attachmentHolder);}
//    }
//    public static class Post extends ObjectHolderClientSyncedEvent {
//        public Post(IAttachmentHolder attachmentHolder) {super(attachmentHolder);}
//    }

}
