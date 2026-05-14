package io.github.daxigua2333.cmagic_clue.data.common;

import io.github.daxigua2333.cmagic_clue.component.ClueObject;
import io.github.daxigua2333.cmagic_clue.data.ObjectsWithLocation;
import io.github.daxigua2333.cmagic_clue.data.location.FromChunkAttachment;
import io.github.daxigua2333.cmagic_clue.data.location.FromEntityAttachment;
import io.github.daxigua2333.cmagic_clue.data.location.FromSavedData;
import io.github.daxigua2333.cmagic_clue.data.location.IRuntimeLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DataManager {
    public static class Common {
        public static ObjectsWithLocation retrieveByBlockPos(Level level, BlockPos pos) {
            IRuntimeLocation fromChunk = new FromChunkAttachment(level, pos);
            return new ObjectsWithLocation(
                    fromChunk,
                    IndexManager.byBlockPos(fromChunk.getHolder(), pos)
            );
        }

        public static ObjectsWithLocation retrieveByEntity(Entity entity) {
            IRuntimeLocation fromEntity = new FromEntityAttachment(entity);
            return new ObjectsWithLocation(
                    fromEntity,
                    new ArrayList<>(fromEntity.getHolder().values())
            );
        }

        // very frequent query in render system, building batch mesh
        public static ObjectsWithLocation retrieveByChunkPos(Level level, ChunkPos chunkPos) {
            // chunk attach
            IRuntimeLocation fromChunk = new FromChunkAttachment(level, chunkPos);
            return new ObjectsWithLocation(
                    fromChunk,
                    new ArrayList<>(fromChunk.getHolder().values())
            );
        }

        // TODO:: some route
        public static List<ClueObject> retrieveAllSavedData(Level level) {
            IRuntimeLocation fromSD = new FromSavedData(level);
            return new ArrayList<>(fromSD.getHolder().values());
        }
    }

    public static class Client extends Common {

        /**
         * USAGE:
         * retrieveByNearbyLoadedChunks(mc.level, mc.player.chunkPosition(), Math.min(radius, Minecraft.getInstance().options.renderDistance().get()));
         */
        public static List<ObjectsWithLocation> retrieveByNearbyLoadedChunks(Level level, ChunkPos center, int radius) {
            List<ObjectsWithLocation> result = new ArrayList<>();

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int chunkX = center.x + x;
                    int chunkZ = center.z + z;
                    // have got ChunkPos
                    if (level.hasChunk(chunkX, chunkZ)) {  // get loaded chunk
                        result.add(retrieveByChunkPos(level, new ChunkPos(chunkX, chunkZ)));
                    }
                }
            }

            return result;
        }

    }

    public static class Server extends Common {
        public static void upsert(IRuntimeLocation location, ClueObject obj) {
            location.getHolder().put(obj);
            location.markDirty();
        }

        public static void delete(IRuntimeLocation location, UUID id) {
            location.getHolder().remove(id);
            location.markDirty();
        }


    }
}
