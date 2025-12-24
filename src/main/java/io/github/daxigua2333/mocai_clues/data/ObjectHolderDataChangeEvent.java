package io.github.daxigua2333.mocai_clues.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public abstract class ObjectHolderDataChangeEvent extends Event {
    @Nullable
    private final IAttachmentHolder attachmentHolder;
    private final RegistryFriendlyByteBuf buf;
    @Nullable
    private final ObjectHolder objectHolder;

    protected ObjectHolderDataChangeEvent(IAttachmentHolder attachmentHolder, RegistryFriendlyByteBuf buf, ObjectHolder objectHolder) {
        this.attachmentHolder = attachmentHolder;
        this.buf = buf;
        this.objectHolder = objectHolder;
    }

    @Nullable
    public ObjectHolder getObjectHolder() {
        return objectHolder;
    }
    public IAttachmentHolder getAttachmentHolder() {
        return attachmentHolder;
    }
    public RegistryFriendlyByteBuf getBuf() {
        return buf;
    }

    public static class Pre extends ObjectHolderDataChangeEvent {
        public Pre(IAttachmentHolder attachmentHolder, RegistryFriendlyByteBuf buf, ObjectHolder objectHolder) {super(attachmentHolder, buf, objectHolder);}
    }
    public static class Post extends ObjectHolderDataChangeEvent {
        public Post(IAttachmentHolder attachmentHolder, RegistryFriendlyByteBuf buf, ObjectHolder objectHolder) {super(attachmentHolder, buf, objectHolder);}
    }

}
