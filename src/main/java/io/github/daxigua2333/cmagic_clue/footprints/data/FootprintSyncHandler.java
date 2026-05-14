package io.github.daxigua2333.cmagic_clue.footprints.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import javax.annotation.Nullable;

public class FootprintSyncHandler implements AttachmentSyncHandler<FootprintMainMap> {

    @Override
    public void write(RegistryFriendlyByteBuf buf,
                      FootprintMainMap attachment,
                      boolean initialSync) {
        if (initialSync) {
            // First time this chunk’s attachment is sent to this client:
            // send the whole map once
            FootprintMainMap.STREAM_CODEC.encode(buf, attachment);

            // we just synced the full state; deltas are now irrelevant
            attachment.clearDeltas();
        } else {
            // Subsequent updates: only send changes
            var upserts = attachment.consumeDirtyUpserts();
            var removals = attachment.consumeDirtyRemovals();

            // If nothing changed since last sync, you can optionally send zeroes.
            // (read() handles it fine.)
            buf.writeVarInt(upserts.size());
            for (var entry : upserts.entrySet()) {
                Vec3 pos = entry.getKey();
                Footprint fp = entry.getValue();

                FootprintMainMap.VEC3_STREAM_CODEC.encode(buf, pos);
                Footprint.STREAM_CODEC.encode(buf, fp);
            }

            buf.writeVarInt(removals.size());
            for (Vec3 pos : removals) {
                FootprintMainMap.VEC3_STREAM_CODEC.encode(buf, pos);
            }
        }
    }

    @Override
    public @Nullable FootprintMainMap read(IAttachmentHolder holder,
                                           RegistryFriendlyByteBuf buf,
                                           @Nullable FootprintMainMap previousValue) {
        // previousValue == null  => client has no data yet => initial sync
        if (previousValue == null) {
            // full map
            return FootprintMainMap.STREAM_CODEC.decode(buf);
        } else {
            // apply delta to existing client state
            int upsertCount = buf.readVarInt();
            for (int i = 0; i < upsertCount; ++i) {
                Vec3 pos = FootprintMainMap.VEC3_STREAM_CODEC.decode(buf);
                Footprint fp = Footprint.STREAM_CODEC.decode(buf);
                previousValue.applyUpdateFromNetwork(pos, fp);
            }

            int removalCount = buf.readVarInt();
            for (int i = 0; i < removalCount; ++i) {
                Vec3 pos = FootprintMainMap.VEC3_STREAM_CODEC.decode(buf);
                previousValue.applyRemovalFromNetwork(pos);
            }

            // keep the same instance, now updated
            return previousValue;
        }
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        // For chunk attachments, NeoForge already restricts to players tracking the chunk;
        // returning true here means “everyone tracking this holder gets it”.
        return true;
    }
}