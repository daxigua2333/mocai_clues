package io.github.daxigua2333.mocai_clues.data;


import io.github.daxigua2333.mocai_clues.MoCaiClues;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.mvstore.MVStoreModule;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ModDatabase {
    private static Nitrite db;

    public static void init() {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            Path modDir    = configDir.resolve(MoCaiClues.MODID);
            Files.createDirectories(modDir);

            Path dbFile = modDir.resolve("data.db");

            MVStoreModule storeModule = MVStoreModule.withConfig()
                    .filePath(dbFile.toString())
                    .compress(true)
                    .build();

            db = Nitrite.builder()
                    .loadModule(storeModule)
                    // If you don’t care about username/password, you can omit them
                    .openOrCreate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to init Nitrite DB", e);
        }
    }

    public static Nitrite getDb() {
        if (db == null || db.isClosed()) {
            throw new IllegalStateException("Nitrite DB not initialized yet");
        }
        return db;
    }

    public static void shutdown() {
        if (db != null && !db.isClosed()) {
            db.close();
        }
    }
}
