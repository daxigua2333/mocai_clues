package io.github.daxigua2333.mocai_clues.data.server.api;

import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.server.ClueObjectHolderInSavedData;
import io.github.daxigua2333.mocai_clues.data.server.ServerDatabase;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.NitriteMyObjectStore;
import io.github.daxigua2333.mocai_clues.data.sync.server.MyObjectSyncServer;
import net.minecraft.world.level.Level;

import java.util.List;

public final class ServerDataAccessor {

    // ======== retrieve =========

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
}
