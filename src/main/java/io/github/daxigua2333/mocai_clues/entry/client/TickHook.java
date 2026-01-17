package io.github.daxigua2333.mocai_clues.entry.client;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.finder.FlashDotSet;
import io.github.daxigua2333.mocai_clues.data.client.ClientDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public final class TickHook {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null || mc.isPaused()) return;
        // don’t spawn every tick; every 4 ticks is plenty for a “flash”
        if ( (level.getGameTime() & 3) != 0 ) return;

        // TODO: maybe change the api in the future, like byDistanceToPlayer. But seems like those out of render distance wont get rendered
        List<ClueObject> data = ClientDataManager.retrieveAllSavedData();
        for (ClueObject obj : data) {
            FlashDotSet compo = obj.getComponent(ComponentType.FLASH_DOT_SET);
            if (compo == null) continue;
            compo.spawn(level);
        }
    }
}
