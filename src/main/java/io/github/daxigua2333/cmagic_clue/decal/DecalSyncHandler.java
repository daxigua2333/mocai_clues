package io.github.daxigua2333.cmagic_clue.decal;

import io.github.daxigua2333.cmagic_clue.decal.client.DecalRenderer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class DecalSyncHandler implements AttachmentSyncHandler<DecalLayerHolder> {

    public DecalSyncHandler() {
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, DecalLayerHolder attachment, boolean initialSync) {
        // Here we only process initial sync, for delta sync we ll use custom packet
        buf.writeBoolean(initialSync);
        if (initialSync) {
            DecalLayerHolder.SYNC_CODEC.encode(buf, attachment);
        }
    }

    @Override
    public @Nullable DecalLayerHolder read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable DecalLayerHolder previousValue) {
        boolean initialSync = buf.readBoolean();
        if (initialSync) {
            DecalLayerHolder decoded = DecalLayerHolder.SYNC_CODEC.decode(buf);

            // dirty build VBO
            if (holder instanceof LevelChunk chunk) {
                DecalRenderer.markDirty(chunk.getPos());
            } else throw new RuntimeException("Wrong IAttachmentHolder class" + holder.getClass());

            return decoded;
        } else {
            return previousValue;
        }
    }

    @Override
    public boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return true;
    }

}
