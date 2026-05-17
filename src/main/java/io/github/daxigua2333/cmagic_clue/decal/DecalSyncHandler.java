package io.github.daxigua2333.cmagic_clue.decal;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.Nullable;

public class DecalSyncHandler implements AttachmentSyncHandler<DecalLayerHolder> {

    public DecalSyncHandler() {
    }

    private static StreamCodec<FriendlyByteBuf, DecalLayerHolder> SYNC_CODEC;

    private static StreamCodec<FriendlyByteBuf, DecalLayerHolder> getSyncCodec() {
        if (SYNC_CODEC == null && DecalAtlasRegistry.ATLAS != null) {
            SYNC_CODEC = DecalLayerHolder.syncCodec(DecalAtlasRegistry.ATLAS);
        }
        return SYNC_CODEC;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf, DecalLayerHolder attachment, boolean initialSync) {
        // Here we only process initial sync, for delta sync we ll use custom packet
        buf.writeBoolean(initialSync);
        if (initialSync) {
            getSyncCodec().encode(buf, attachment);
        }
    }

    @Override
    public @Nullable DecalLayerHolder read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable DecalLayerHolder previousValue) {
        boolean initialSync = buf.readBoolean();
        if (initialSync) {
            DecalLayerHolder decoded = getSyncCodec().decode(buf);

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
