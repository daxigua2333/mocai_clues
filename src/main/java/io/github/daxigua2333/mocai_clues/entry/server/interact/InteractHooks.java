package io.github.daxigua2333.mocai_clues.entry.server.interact;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.world.interact.BaseInteractHandler;
import io.github.daxigua2333.mocai_clues.data.server.api.ServerDataAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Collection;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public class InteractHooks {

    @SubscribeEvent
    public static void onLookingAt(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        // raytrace: player.pick(range, partialTicks, ClipContext.Fluid.NONE)
        HitResult hr = player.pick(Config.COMMON.FINDER_HIT_DISTANCE.get(), 0.0F, false);  // TODO: config

        Collection<ClueObject> data = new ArrayList<>();

        switch (hr.getType()) {
            case BLOCK -> {
                BlockHitResult bhr = (BlockHitResult) hr;
                BlockPos pos = bhr.getBlockPos();
                // optional: check which face was hit: bhr.getDirection()
                data = ServerDataAccessor.retrieveByBlockPos(pos);
            }
            case ENTITY -> {
                EntityHitResult ehr = (EntityHitResult) hr;
                Entity e = ehr.getEntity();
                data = ServerDataAccessor.retrieveByEntity(e);
            }
            default -> {
                return;
            }
        }

        for (var obj : data) {
            for (ClueComponent compo : obj.getComponents()) {
                if (compo instanceof BaseInteractHandler iCompo) {
                    iCompo.onHandle(new BaseInteractHandler.Context(player));
                }
            }
        }

    }

//    @SubscribeEvent
//    public static void onMoving() {
//
//    }
}
