package io.github.daxigua2333.mocai_clues.entry.common;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventHolder;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;
import io.github.daxigua2333.mocai_clues.data.client.api.ClientAccessor;
import io.github.daxigua2333.mocai_clues.data.server.api.ServerDataAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public class InteractHooks {

    @SubscribeEvent
    public static void onLookingAt(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        HitResult hr;
        if (level.isClientSide) {
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
                data = level.isClientSide ? ClientAccessor.retrieveByBlockPos(pos) : ServerDataAccessor.retrieveByBlockPos(level, pos);
            }
            case ENTITY -> {
                EntityHitResult ehr = (EntityHitResult) hr;
                Entity e = ehr.getEntity();
                data = level.isClientSide ? ClientAccessor.retrieveByEntity(e) : ServerDataAccessor.retrieveByEntity(e);
            }
            default -> {
                return;
            }
        }

        var builder = new InteractEvent.Context.Builder(InteractEventRegistry.EntryType.RAY_TRACE);
        triggerEvents(data, builder.player(player));
    }


    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        Player player = event.getEntity();
        Level level = event.getLevel();

        List<ClueObject> data = level.isClientSide ? ClientAccessor.retrieveByBlockPos(pos) : ServerDataAccessor.retrieveByBlockPos(level, pos);

        var builder = new InteractEvent.Context.Builder(InteractEventRegistry.EntryType.CLICK);
        triggerEvents(data, builder.player(player));
    }

    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        Player player = event.getEntity();
        Entity target = event.getTarget();
        Level level = event.getLevel();

        List<ClueObject> data = level.isClientSide ? ClientAccessor.retrieveByEntity(target) : ServerDataAccessor.retrieveByEntity(target);

        var builder = new InteractEvent.Context.Builder(InteractEventRegistry.EntryType.CLICK);
        triggerEvents(data, builder.player(player));
    }


//    @SubscribeEvent
//    public static void onMoving() {
//
//    }

    private static void triggerEvents(List<ClueObject> data, InteractEvent.Context.Builder contextBuilder) {
        for (var obj : data) {
            InteractEventHolder compo = obj.getComponent(ComponentType.INTERACT_EVENT_HOLDER);
            if (compo == null) return;
            for (InteractEvent e : compo.getImmutable()) {
                e.onHandle(contextBuilder.object(obj).build());
            }
        }

    }

}
