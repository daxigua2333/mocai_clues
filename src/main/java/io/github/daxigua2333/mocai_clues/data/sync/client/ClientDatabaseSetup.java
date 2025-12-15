package io.github.daxigua2333.mocai_clues.data.sync.client;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.data.client.ClientDatabase;
import io.github.daxigua2333.mocai_clues.data.sync.ClueObjectKeyProvider;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.items.ClueFinderItem;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;

@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public class ClientDatabaseSetup {

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientDatabase::init);
        event.enqueueWork( () -> {
            MyObjectSync.initClient(ClientDatabase.get(), new ClueObjectKeyProvider());
            MyObjectSync.client().store().createClueFieldIndex(IndexOptions.indexOptions(IndexType.UNIQUE));
            MyObjectSync.client().store().createClueFieldIndex(IndexOptions.indexOptions(IndexType.NON_UNIQUE),
                    "type");
        });
    }

}
