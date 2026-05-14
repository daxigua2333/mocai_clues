package io.github.daxigua2333.cmagic_clue;

import io.github.daxigua2333.cmagic_clue.component.system.discovery.FinderTick;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = CMagicClue.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CMagicClue.MODID, value = Dist.CLIENT)
public class CMagicClueClient {
    public CMagicClueClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CMagicClue.LOGGER.info("HELLO FROM CLIENT SETUP");
        CMagicClue.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(FinderTick::registerTextureChange);
    }

}
