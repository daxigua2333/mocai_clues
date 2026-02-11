package io.github.daxigua2333.mocai_clues.data.common;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.location.FromChunkAttachment;
import io.github.daxigua2333.mocai_clues.data.location.FromEntityAttachment;
import io.github.daxigua2333.mocai_clues.data.location.FromSavedData;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DataManager {
    public static class Common {
        public static RetrieveResult retrieveByBlockPos(Level level, BlockPos pos) {
            IRuntimeLocation fromChunk = new FromChunkAttachment(level, pos);
            return new RetrieveResult(
                    fromChunk,
                    IndexManager.byBlockPos(fromChunk.getHolder(), pos)
            );
        }

        public static RetrieveResult retrieveByEntity(Entity entity) {
            IRuntimeLocation fromEntity = new FromEntityAttachment(entity);
            return new RetrieveResult(
                    fromEntity,
                    new ArrayList<>(fromEntity.getHolder().values())
            );
        }

        // very frequent query in render system, building batch mesh
        public static RetrieveResult retrieveByChunkPos(Level level, ChunkPos chunkPos) {
            // chunk attach
            IRuntimeLocation fromChunk = new FromChunkAttachment(level, chunkPos);
            return new RetrieveResult(
                    fromChunk,
                    new ArrayList<>(fromChunk.getHolder().values())
            );
        }

        // TODO:: some route
        public static List<ClueObject> retrieveAllSavedData(Level level) {
            IRuntimeLocation fromSD = new FromSavedData(level);
            return new ArrayList<>(fromSD.getHolder().values());
        }
        // TODO: also (on client) some distance culling, and traverse nearby chunks and get attachments

    }

    public static class Client extends Common {

        /**
         * USAGE:
         * retrieveByNearbyLoadedChunks(mc.level, mc.player.chunkPosition(), Math.min(radius, Minecraft.getInstance().options.renderDistance().get()));
         */
        public static RetrieveResult retrieveByNearbyLoadedChunks(Level level, ChunkPos center, int radius) {
            RetrieveResult result = null;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int chunkX = center.x + x;
                    int chunkZ = center.z + z;
                    // have got ChunkPos
                    if (level.hasChunk(chunkX, chunkZ)) {  // get loaded chunk
                        if (result == null) {
                            result = retrieveByChunkPos(level, new ChunkPos(chunkX, chunkZ));
                        } else {
                            result.addAll(retrieveByChunkPos(level, new ChunkPos(chunkX, chunkZ)));
                        }
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
