package io.github.daxigua2333.mocai_clues.data.sync;


import io.github.daxigua2333.mocai_clues.data.sync.client.MyObjectSyncClient;
import io.github.daxigua2333.mocai_clues.data.sync.misc.MyObjectKeyProvider;
import io.github.daxigua2333.mocai_clues.data.sync.server.MyObjectSyncServer;
import org.dizitart.no2.Nitrite;

import java.util.Objects;

public final class MyObjectSync {
    private MyObjectSync() {}

    private static volatile MyObjectSyncServer SERVER;
    private static volatile MyObjectSyncClient CLIENT;

    public static void initServer(Nitrite db, MyObjectKeyProvider keyProvider) {
        Objects.requireNonNull(db);
        Objects.requireNonNull(keyProvider);
        SERVER = new MyObjectSyncServer(new NitriteMyObjectStore(db, NitriteMyObjectStore.Mode.SERVER, keyProvider));
    }

    public static void initClient(Nitrite db, MyObjectKeyProvider keyProvider) {
        Objects.requireNonNull(db);
        Objects.requireNonNull(keyProvider);
        CLIENT = new MyObjectSyncClient(new NitriteMyObjectStore(db, NitriteMyObjectStore.Mode.CLIENT, keyProvider));
    }

    public static MyObjectSyncServer server() {
        MyObjectSyncServer s = SERVER;
        if (s == null) throw new IllegalStateException("MyObjectSync server not initialized");
        return s;
    }

    public static MyObjectSyncClient client() {
        MyObjectSyncClient c = CLIENT;
        if (c == null) throw new IllegalStateException("MyObjectSync client not initialized");
        return c;
    }

    public static boolean isServerInitialized() {
        return SERVER != null;
    }

    public static boolean isClientInitialized() {
        return CLIENT != null;
    }
}
