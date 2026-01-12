package io.github.daxigua2333.mocai_clues.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;

import java.util.UUID;
import java.util.function.Function;

// TODO: emm.... I think this should be default running on main thread to avoid concurrency issues... but who knows
public final class ObjectHolderSyncHandler<T> implements AttachmentSyncHandler<ObjectHolder<T>> {

    private final Function<T, UUID> idGetter;
    private final Codec<T> elementCodec;
    private final StreamCodec<ByteBuf, T> elementStreamCodec;

    public ObjectHolderSyncHandler(Function<T, UUID> idGetter,
                                   Codec<T> elementCodec,
                                   StreamCodec<ByteBuf, T> elementStreamCodec) {
        this.idGetter = idGetter;
        this.elementCodec = elementCodec;
        this.elementStreamCodec = elementStreamCodec;
    }

    /** Convenience: full persistence codec for the attachment. */
    public Codec<ObjectHolder<T>> codec() {
        return ObjectHolder.codec(idGetter, elementCodec, elementStreamCodec);
    }

    /** Convenience: full stream codec (no deltas). */
    public StreamCodec<ByteBuf, ObjectHolder<T>> streamCodec() {
        return ObjectHolder.streamCodec(idGetter, elementCodec, elementStreamCodec);
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, ObjectHolder<T> attachment, boolean initialSync) {
        if (initialSync) {
            // First time this attachment is synced to this client: send full map.
            attachment.encodeFull(buf);
        } else {
            // Later: send only changes.
            attachment.encodeDelta(buf);
        }
        // We’ve flushed the changes for this side.
        attachment.resetChangeTracking();
    }

    @Override
    public ObjectHolder<T> read(IAttachmentHolder holder,
                                 RegistryFriendlyByteBuf buf,
                                 ObjectHolder<T> previousValue) {
        if (previousValue == null) {
            // Client had no prior data for this attachment, so we expect a full payload.
            ObjectHolder<T> map = new ObjectHolder<>(idGetter, elementCodec, elementStreamCodec);
            map.decodeFull(buf);

            NeoForge.EVENT_BUS.post(new ObjectHolderClientSyncedEvent.Full<>(holder, map));
            return map;
        } else {
            // Client already has data; apply delta.
            ObjectHolder.DeltaPayload<T> delta = ObjectHolder.DeltaPayload.deltaStreamCodec(elementStreamCodec).decode(buf);
            NeoForge.EVENT_BUS.post(new ObjectHolderClientSyncedEvent.Delta<>(holder, previousValue, delta));

//            previousValue.applyDelta(buf);
            previousValue.applyDeltaPayload(delta);
            return previousValue;
        }
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        // Example: just sync to everyone tracking the holder.
        // If you need per-player visibility control, implement it here.
        return true;
    }
}
