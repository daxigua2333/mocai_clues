package io.github.daxigua2333.mocai_clues.items.statics;

import io.github.daxigua2333.mocai_clues.data_attachments.ClueContainerMap;
import io.github.daxigua2333.mocai_clues.data_attachments.ModDataAttachmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * High-performance search helpers for Inventory attachments stored in ChunkInventoryMap.
 *
 * IMPORTANT: This only scans chunk-local indexes (ChunkInventoryMap). For best perf make sure
 * any attachment (including those attached to BlockEntity) is inserted into the chunk map
 * when created/changed.
 */
public final class ClueContainerSearchUtils {   // TODO: performance issues
    private ClueContainerSearchUtils() {}

    /**
     * Fast check whether *any* attachment exists within radius (spherical) of center.
     * This version does NOT load chunks; it only inspects loaded chunks.
     *
     * @param level  server level (should be server-side)
     * @param center search center (world coordinates)
     * @param radius radius in blocks (>=0)
     * @return true if at least one attached block is within radius
     */
    public static boolean anyAttachmentInRadius(Level level, Vec3 center, int radius) {
        return anyAttachmentInRadius(level, center, radius, /*loadChunks=*/false);
    }

    /**
     * Overload: optionally allow loading chunks (slower).
     *
     * @param loadChunks if true, may load chunks in the search area (avoid in hot loops!)
     */
    public static boolean anyAttachmentInRadius(Level level, Vec3 center, int radius, boolean loadChunks) {
        if (radius < 0) return false;
        final long rr = (long) radius * radius;

        final int minChunkX = floorDiv((int) Math.floor(center.x) - radius, 16);
        final int maxChunkX = floorDiv((int) Math.floor(center.x) + radius, 16);
        final int minChunkZ = floorDiv((int) Math.floor(center.z) - radius, 16);
        final int maxChunkZ = floorDiv((int) Math.floor(center.z) + radius, 16);

        // cache center components once (avoid allocations)
        final double cx = center.x;
        final double cy = center.y;
        final double cz = center.z;

        for (int cxChunk = minChunkX; cxChunk <= maxChunkX; cxChunk++) {
            for (int czChunk = minChunkZ; czChunk <= maxChunkZ; czChunk++) {
                LevelChunk chunk = getChunk(level, cxChunk, czChunk, loadChunks);
                if (chunk == null) continue;

                ClueContainerMap map = chunk.getData(ModDataAttachmentRegistry.CLUE_CONTAINER_MAP);
//                if (map == null) continue;

                List<BlockPos> keys = map.getKeys();
                if (keys == null || keys.isEmpty()) continue;

                for (BlockPos pos : keys) {
                    // fast squared-distance (use block center => add 0.5 if desired; omitted for speed)
                    double dx = pos.getX() - cx;
                    double dy = pos.getY() - cy;
                    double dz = pos.getZ() - cz;
                    if ((dx * dx + dy * dy + dz * dz) <= rr) {
                        return true; // immediate exit on first found
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return a list of BlockPos for all attachments within radius of center.
     * This method inspects loaded chunks only by default (no chunk loading).
     *
     * @param level  server level
     * @param center center point
     * @param radius radius in blocks
     * @return non-null list (may be empty). Order is not guaranteed.
     */
    public static List<BlockPos> findAttachmentsInRadius(Level level, Vec3 center, int radius) {
        return findAttachmentsInRadius(level, center, radius, /*loadChunks=*/false);
    }

    public static List<BlockPos> findAttachmentsInRadius(Level level, Vec3 center, int radius, boolean loadChunks) {
        List<BlockPos> out = new ArrayList<>();
        if (radius < 0) return out;
        final long rr = (long) radius * radius;

        final int minChunkX = floorDiv((int) Math.floor(center.x) - radius, 16);
        final int maxChunkX = floorDiv((int) Math.floor(center.x) + radius, 16);
        final int minChunkZ = floorDiv((int) Math.floor(center.z) - radius, 16);
        final int maxChunkZ = floorDiv((int) Math.floor(center.z) + radius, 16);

        final double cx = center.x;
        final double cy = center.y;
        final double cz = center.z;

        for (int cxChunk = minChunkX; cxChunk <= maxChunkX; cxChunk++) {
            for (int czChunk = minChunkZ; czChunk <= maxChunkZ; czChunk++) {
                LevelChunk chunk = getChunk(level, cxChunk, czChunk, loadChunks);
                if (chunk == null) continue;

                ClueContainerMap map = chunk.getData(ModDataAttachmentRegistry.CLUE_CONTAINER_MAP);
//                if (map == null) continue;

                List<BlockPos> keys = map.getKeys();
                if (keys == null || keys.isEmpty()) continue;
                for (BlockPos pos : keys) {
                    double dx = pos.getX() - cx;
                    double dy = pos.getY() - cy;
                    double dz = pos.getZ() - cz;
                    if ((dx * dx + dy * dy + dz * dz) <= rr) {
                        out.add(pos);
                    }
                }
            }
        }
        return out;
    }

    // -------- helpers --------

    private static LevelChunk getChunk(Level level, int chunkX, int chunkZ, boolean loadIfMissing) {
        // NOTE: Method names vary by mappings. The idea:
        // - if loadIfMissing is false: return null for unloaded chunks (fast)
        // - if loadIfMissing is true: return the chunk (may load)
        try {
            if (!loadIfMissing) {
                if (!level.hasChunk(chunkX, chunkZ)) return null;
                return level.getChunk(chunkX, chunkZ);
            } else {
                return level.getChunk(chunkX, chunkZ); // may load chunk (slower)
            }
        } catch (NoSuchMethodError | AbstractMethodError e) {
            // Fallback: attempt to use getChunk (may load); tolerable but slower
            return level.getChunk(chunkX, chunkZ);
        }
    }

    private static int floorDiv(int a, int b) {
        // Java's / is fine for positive values but keep a reliable integer floorDiv
        int div = a / b;
        if ((a ^ b) < 0 && (a % b != 0)) div--;
        return div;
    }
}
