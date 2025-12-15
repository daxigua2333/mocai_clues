package io.github.daxigua2333.mocai_clues.data.sync.server;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.data.server.ServerDatabase;
import io.github.daxigua2333.mocai_clues.data.sync.misc.ClueObjectKeyProvider;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class ServerDatabaseSetup {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MoCaiClues.LOGGER.info("========== server starting ============");
        ServerDatabase.init(event.getServer());
        MyObjectSync.initServer(ServerDatabase.get(), new ClueObjectKeyProvider());
        MyObjectSync.server().store().createClueFieldIndex(IndexOptions.indexOptions(IndexType.UNIQUE));
        MyObjectSync.server().store().createClueFieldIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE));
    }

    @SubscribeEvent
    private static void onServerStopping(ServerStoppingEvent event) {
        MoCaiClues.LOGGER.info("========== server shutting down ============");
        ServerDatabase.shutdown();
    }

}
