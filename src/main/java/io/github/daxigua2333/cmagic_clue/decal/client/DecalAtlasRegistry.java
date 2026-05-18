package io.github.daxigua2333.cmagic_clue.decal.client;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Register in client reload resource event
 */
@EventBusSubscriber(modid = CMagicClue.MODID, value = Dist.CLIENT)
public final class DecalAtlasRegistry {
    public static DecalAtlas ATLAS;  // singleton
    public static final ResourceLocation ATLAS_ID = ResourceLocation.fromNamespaceAndPath(
            CMagicClue.MODID, "textures/atlas/layer_atlas.png");

    public enum StaticTextures {
        BLOODSTAIN1("textures/misc/bloodstain1.png"),
        BLOODSTAIN2("textures/misc/bloodstain2.png"),
        BLOODSTAIN3("textures/misc/bloodstain3.png"),
        BLOODSTAIN4("textures/misc/bloodstain4.png"),
        BLOODSTAIN5("textures/misc/bloodstain5.png"),
        BLOODSTAIN6("textures/misc/bloodstain6.png"),
        ;

        private final ResourceLocation id;

        public ResourceLocation getId() {
            return id;
        }

        StaticTextures(String path) {
            this.id = ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, path);
        }
    }


    @SubscribeEvent
    public static void onReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                // first create
                if (ATLAS == null) {
                    ATLAS = new DecalAtlas(ATLAS_ID);
                    ATLAS.register(Minecraft.getInstance().getTextureManager());
                }

                List<ResourceLocation> list = new ArrayList<>(StaticTextures.values().length);
                for (StaticTextures e : StaticTextures.values()) list.add(e.getId());

                ATLAS.freeStatic();
                ATLAS.initStatic(resourceManager, list);
            }
        });
    }
}
