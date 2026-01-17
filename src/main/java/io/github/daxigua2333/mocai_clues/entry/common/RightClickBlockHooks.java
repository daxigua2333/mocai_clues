package io.github.daxigua2333.mocai_clues.entry.common;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.finder.ClickWithFinder;
import io.github.daxigua2333.mocai_clues.data.client.api.ClientAccessor;
import io.github.daxigua2333.mocai_clues.data.server.api.ServerDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class RightClickBlockHooks {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;  // in case it trigger twice
        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        Player player = event.getEntity();
        Level level = event.getLevel();

        if (!level.isClientSide) {
            List<ClueObject> data = ServerDataAccessor.retrieveByBlockPos(level, pos);
            for (ClueObject obj : data) {
                ClickWithFinder compo = obj.getComponent(ComponentType.SEND_CLUE);
                if (compo == null) continue;
                compo.send((ServerPlayer) player, pos);  // TODO: merge, send message
            }
        }
    }

}
