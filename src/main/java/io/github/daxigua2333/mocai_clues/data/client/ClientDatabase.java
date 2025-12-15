package io.github.daxigua2333.mocai_clues.data.client;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class ClientDatabase {

    private static Nitrite db;

    private ClientDatabase() {}

    public static void init() {
        if (db != null && !db.isClosed()) {
            return;
        }

        // Only ever called on physical client (MyModClient)
        File gameDir = Minecraft.getInstance().gameDirectory;
        Path dbDir = gameDir.toPath().resolve(MoCaiClues.MODID);

        try {
            Files.createDirectories(dbDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create client DB directory: " + dbDir, e);
        }

        Path dbFile = dbDir.resolve("client.nitrite");

        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath(dbFile.toString())
                .compress(true)
                .build();

        db = Nitrite.builder()
                .loadModule(storeModule)
                .openOrCreate();

    }

    public static Nitrite get() {
        if (db == null || db.isClosed()) {
            throw new IllegalStateException("Client database not initialized yet.");
        }
        return db;
    }

    public static void shutdown() {
        if (db != null && !db.isClosed()) {
            db.close();
            db = null;
        }
    }
}
