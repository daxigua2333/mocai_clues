package io.github.daxigua2333.mocai_clues.footprints.statics;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.footprints.Events.BlockBecameAirEvent;
import io.github.daxigua2333.mocai_clues.footprints.Events.BlockBecameNonAirEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;

import java.util.List;

@EventBusSubscriber(modid = MoCaiClues.MODID)
public final class DeleteEventHooks {
//    // ─────────────────────────────────────────────────────────────────────────────
//    // 1) BLOCK BECOMES AIR  ->  remove footprints in block ABOVE (y+1)
//    // ─────────────────────────────────────────────────────────────────────────────
//
//    /**
//     * Player breaks a block in survival/creative.
//     * After this resolves (and if not cancelled), the block will be replaced by air. :contentReference[oaicite:1]{index=1}
//     */
//    @SubscribeEvent
//    public static void onBlockBreak(BlockEvent.BreakEvent event) {
//        if (event.isCanceled()) return; // nothing actually breaks
//
//        LevelAccessor level = event.getLevel();
//        BlockPos pos = event.getPos();
//
//        onBlockBecameAir(level, pos);
//    }
//
//    /**
//     * Explosion removes blocks: they are usually turned into air. :contentReference[oaicite:2]{index=2}
//     */
//    @SubscribeEvent
//    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
//        LevelAccessor level = event.getLevel();
//
//        for (BlockPos pos : event.getAffectedBlocks()) {
//            onBlockBecameAir(level, pos);
//        }
//    }
//
//    // (Optional) Piston moves: blocks that get *destroyed* by a piston become air.
//    @SubscribeEvent
//    public static void onPistonPost(PistonEvent.Post event) {
//        LevelAccessor level = event.getLevel();
//        var helper = event.getStructureHelper();
//        if (helper == null) return;
//
//        // Blocks in getToDestroy() are broken by piston.
//        for (BlockPos destroyed : helper.getToDestroy()) {
//            onBlockBecameAir(level, destroyed);
//        }
//
//        // For blocks in getToPush(), the original position becomes air.
//        for (BlockPos from : helper.getToPush()) {
//            onBlockBecameAir(level, from);
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // 2) BLOCK GOES FROM AIR -> NON-AIR  ->  remove footprints IN THAT BLOCK
//    // ─────────────────────────────────────────────────────────────────────────────
//
//    /**
//     * Player (or entity) places a block. :contentReference[oaicite:3]{index=3}
//     */
//    @SubscribeEvent
//    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
//        if (event.isCanceled()) return;
//
//        LevelAccessor level = event.getLevel();
//        BlockPos pos = event.getPos();
//
//        BlockState oldState = event.getBlockSnapshot().getState();  // replaced state
//        BlockState newState = event.getPlacedBlock();               // block being placed
//
//        if (oldState.isAir() && !newState.isAir()) {
//            onBlockBecameNonAir(level, pos);
//        }
//    }
//
//    /**
//     * Multi-block placements (beds, doors, etc.) use EntityMultiPlaceEvent with snapshots
//     * for every replaced position. :contentReference[oaicite:4]{index=4}
//     */
//    @SubscribeEvent
//    public static void onMultiBlockPlaced(BlockEvent.EntityMultiPlaceEvent event) {
//        if (event.isCanceled()) return;
//
//        LevelAccessor level = event.getLevel();
//        List<BlockSnapshot> snapshots = event.getReplacedBlockSnapshots();
//
//        for (BlockSnapshot snapshot : snapshots) {
//            BlockPos pos = snapshot.getPos();
//            BlockState oldState = snapshot.getState();          // previous state
//            BlockState newState = snapshot.getCurrentState();   // live world state now
//
//            if (oldState.isAir() && !newState.isAir()) {
//                onBlockBecameNonAir(level, pos);
//            }
//        }
//    }
//
//    /**
//     * Fluids placing blocks (water/lava + cobblestone/obsidian, water logging, etc.). :contentReference[oaicite:5]{index=5}
//     */
//    @SubscribeEvent
//    public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
//        if (event.isCanceled()) return;
//
//        LevelAccessor level = event.getLevel();
//        BlockPos pos = event.getPos();
//
//        BlockState oldState = event.getOriginalState();
//        BlockState newState = event.getNewState();
//
//        if (oldState.isAir() && !newState.isAir()) {
//            onBlockBecameNonAir(level, pos);
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // 3) Helpers that actually remove footprints
//    // ─────────────────────────────────────────────────────────────────────────────
//
//    /**
//     * Case 1:
//     *   A block at {@code pos} changed into air.
//     *   We remove all footprints whose feet position lies in the block ABOVE it (y+1).
//     */
//    private static void onBlockBecameAir(LevelAccessor level, BlockPos pos) {
//        if (level.isClientSide()) return;
//        BlockPos above = pos.above();
//        MoCaiClues.LOGGER.debug("on become air at {}, {}", pos, above);
//        FootprintServerHelper.deleteByBlockPos((Level) level, above);
//    }
//
//    /**
//     * Case 2:
//     *   A block at {@code pos} changed from air to some non-air state (solid, fluid, etc.).
//     *   We remove all footprints whose feet position lies inside that block.
//     */
//    private static void onBlockBecameNonAir(LevelAccessor level, BlockPos pos) {
//        if (level.isClientSide()) return;
//        MoCaiClues.LOGGER.debug("on become non-air at {}", pos);
//        FootprintServerHelper.deleteByBlockPos((Level) level, pos);
//    }

    @SubscribeEvent
    public static void onBecameAir(BlockBecameAirEvent event) {
        // event.getLevel(), event.getPos(), event.getOld(), event.getNew()
//        MoCaiClues.LOGGER.debug("on became air");
        FootprintServerHelper.deleteByBlockPos(event.getLevel(), event.getPos().above());

    }

    @SubscribeEvent
    public static void onBecameNonAir(BlockBecameNonAirEvent event) {
        // Example: spawn particles, update your own data structures, etc.
//        MoCaiClues.LOGGER.debug("on became non-air");
        FootprintServerHelper.deleteByBlockPos(event.getLevel(), event.getPos());
    }
}
