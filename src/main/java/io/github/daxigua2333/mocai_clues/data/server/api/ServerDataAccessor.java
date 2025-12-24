package io.github.daxigua2333.mocai_clues.data.server.api;

import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.server.ServerDatabase;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.NitriteMyObjectStore;
import io.github.daxigua2333.mocai_clues.data.sync.server.MyObjectSyncServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

// TODO: route to SD or something...
public final class ServerDataAccessor {

    // ======== retrieve =========
    // TODO: mutable and mark dirty issues......
    public static ClueObject retrieveByIDInSD(MinecraftServer server, UUID id) {
        return ClueObjectHolderInSavedData.getInstance(server).holder().get(id);
    }

    // ========== upsert ==========
    public static void createDefault() {
        var obj = Assembler.createManualClue();
        NitriteMyObjectStore store = MyObjectSync.server().store();
        store.serverUpsert(obj);
    }

    public static void createDefault(Level level) {
        var obj = Assembler.createManualClue();
        var holder = ClueObjectHolderInSavedData.getInstance(level.getServer());
        holder.put(obj);
    }

    public static void upsertInSD(MinecraftServer server, ClueObject obj) {
        ClueObjectHolderInSavedData.getInstance(server).put(obj);
    }

    public static void deleteInSD(MinecraftServer server, UUID id) {
        ClueObjectHolderInSavedData.getInstance(server).remove(id);
    }
}
