package io.github.daxigua2333.cmagic_clue.decal;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.data.ModAttachmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Random;

@EventBusSubscriber(modid = CMagicClue.MODID)
public class ServerLogicHooks {

    private static final Random random = new Random();

    @SubscribeEvent
    public static void onEntityHurt(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // TODO: check opera state

        BlockPos pos = player.getOnPos();
        ServerLevel level = (ServerLevel) player.level();
        DecalLayerHolder holder = level.getChunk(pos).getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER);

        var textures = DecalAtlasRegistry.StaticTextures.values();
        holder.pushLayerAt(level, pos, Direction.UP, textures[random.nextInt(textures.length)].getId());

    }


}
