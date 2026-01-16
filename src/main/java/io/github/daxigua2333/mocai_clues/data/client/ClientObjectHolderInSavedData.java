package io.github.daxigua2333.mocai_clues.data.client;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderClientSyncedEvent;
import io.github.daxigua2333.mocai_clues.data.ObjectHolderDataChangeEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.NeoForge;

@OnlyIn(Dist.CLIENT)
public final class ClientObjectHolderInSavedData {
    private static final ClientObjectHolderInSavedData INSTANCE = new ClientObjectHolderInSavedData();

    private ObjectHolder<ClueObject> holder = new ObjectHolder<>(
        ClueObject::getId,
        ClueObject.CODEC,
        ClueObject.STREAM_CODEC
    );

    private ClientObjectHolderInSavedData() {
        // maybe send a packet to ask for full data, or hook into player login event and sync

    }

    public static ClientObjectHolderInSavedData getInstance() {
        return INSTANCE;
    }

    public ObjectHolder<ClueObject> getHolder() {
        return holder;
    }

    // Called from networking handlers:
    public void replaceAll(ObjectHolder<ClueObject> fullCopy) {
//        holder.clear();
//        fullCopy.forEachValue(holder::put); // however your API looks
//        holder.resetChangeTracking();       // client should track changes only if you want UI diff, etc.
        this.holder = fullCopy;  // I think this replaceAll process has been done in the decode process
        ClientIndexManager.savedDataEnsure(holder);
        NeoForge.EVENT_BUS.post(new ObjectHolderClientSyncedEvent.Full<>(null, fullCopy));
    }

    public void applyDelta(ObjectHolder.DeltaPayload<ClueObject> payload) {
        NeoForge.EVENT_BUS.post(new ObjectHolderClientSyncedEvent.Delta<>(null, holder, payload));
        holder.applyDeltaPayload(payload);
    }
}
