package io.github.daxigua2333.mocai_clues.entry.server.interact;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventHolder;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.FinderHit;
import io.github.daxigua2333.mocai_clues.data.client.api.ClientAccessor;
import io.github.daxigua2333.mocai_clues.data.server.api.ServerDataAccessor;
import net.minecraft.client.Minecraft;
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
import java.util.List;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public class InteractHooks {

    @SubscribeEvent
    public static void onLookingAt(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        HitResult hr;
        boolean isClientSide = player.level().isClientSide;
        if (isClientSide) {
            hr = Minecraft.getInstance().hitResult;
        } else {
            // raytrace: player.pick(range, partialTicks, ClipContext.Fluid.NONE)
            hr = player.pick(Config.COMMON.FINDER_HIT_DISTANCE.get(), 0.0F, false);  // TODO: config
        }
        if (hr == null) return;

        List<ClueObject> data;

        switch (hr.getType()) {
            case BLOCK -> {
                BlockHitResult bhr = (BlockHitResult) hr;
                BlockPos pos = bhr.getBlockPos();
                // optional: check which face was hit: bhr.getDirection()
                data = isClientSide ? ClientAccessor.retrieveByBlockPos(pos) : ServerDataAccessor.retrieveByBlockPos(pos);
            }
            case ENTITY -> {
                EntityHitResult ehr = (EntityHitResult) hr;
                Entity e = ehr.getEntity();
                data = isClientSide ? ClientAccessor.retrieveByEntity(e) : ServerDataAccessor.retrieveByEntity(e);
            }
            default -> {
                return;
            }
        }

        InteractEvent.Context context = new InteractEvent.Context(InteractEventRegistry.EntryType.RAY_TRACE, player);
        for (var obj : data) {
            InteractEventHolder compo = obj.getComponent(ComponentType.INTERACT_EVENT_HOLDER);
            if (compo == null) return;
            for (InteractEvent e : compo.getImmutable()) {
                e.onHandle(context);
            }
        }

    }

//    @SubscribeEvent
//    public static void onMoving() {
//
//    }
}
