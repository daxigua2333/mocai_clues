package io.github.daxigua2333.mocai_clues.data.server;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ServerDatabase {

    private static Nitrite db;

    private ServerDatabase() {

    }

    public static void init(MinecraftServer server) {
        if (db != null && !db.isClosed()) {
            return;
        }

        // Put the DB in the current world save folder: <world>/mymod/
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        Path dbDir = worldRoot.resolve(MoCaiClues.MODID);

        try {
            Files.createDirectories(dbDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create server DB directory: " + dbDir, e);
        }

        Path dbFile = dbDir.resolve("server.nitrite");

        // Configure MVStore-backed storage
        MVStoreModule storeModule = MVStoreModule.withConfig()
                .filePath(dbFile.toString())
                .compress(true)
                .build();

        db = Nitrite.builder()
                .loadModule(storeModule)
                // Add user/pass if you want encryption:
                // .openOrCreate("user", "password");
                .openOrCreate();
    }

    public static Nitrite get() {
        if (db == null || db.isClosed()) {
            throw new IllegalStateException("Server database not initialized yet.");
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
