package io.github.daxigua2333.mocai_clues.data.common;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.world.data.AttachedEntitySet;
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
        public static List<RetrieveResult> retrieveByBlockPos(Level level, BlockPos pos) {
            List<RetrieveResult> result = new ArrayList<>();
            // chunk attach
            IRuntimeLocation fromChunk = new FromChunkAttachment(level, pos);
            result.add(new RetrieveResult(
                    fromChunk,
                    IndexManager.byBlockPos(fromChunk.getHolder(), pos)
            ));
            // saved data
            IRuntimeLocation fromSD = new FromSavedData(level);
            result.add(new RetrieveResult(
                    fromSD,
                    IndexManager.byBlockPos(fromSD.getHolder(), pos)
            ));

            return result;
        }

        public static List<RetrieveResult> retrieveByEntity(Entity entity) {
            List<RetrieveResult> result = new ArrayList<>();
            // entity attachment
            IRuntimeLocation fromEntity = new FromEntityAttachment(entity);
            result.add(new RetrieveResult(
                    fromEntity,
                    new ArrayList<>(fromEntity.getHolder().values())
            ));

            // TODO: optimize: I think there is no need to boost this by index
            List<ClueObject> objs = new ArrayList<>();
            IRuntimeLocation fromSD = new FromSavedData(entity.level());
            for (ClueObject obj : fromSD.getHolder().values()) {
                AttachedEntitySet compo = obj.getComponent(ComponentType.ATTACHED_ENTITY_SET);
                if (compo == null) continue;
                if (compo.getImmutable().contains(entity.getUUID())) {
                    objs.add(obj);
                }
            }

            result.add(new RetrieveResult(
                    fromSD,
                    objs
            ));

            return result;
        }

        // very frequent query in render system, building batch mesh
        public static List<RetrieveResult> retrieveByChunkPos(Level level, ChunkPos chunkPos) {
            List<RetrieveResult> result = new ArrayList<>();
            // chunk attach
            IRuntimeLocation fromChunk = new FromChunkAttachment(level, chunkPos);
            result.add(new RetrieveResult(
                    fromChunk,
                    new ArrayList<>(fromChunk.getHolder().values())
            ));
            // saved data
            IRuntimeLocation fromSD = new FromSavedData(level);
            result.add(new RetrieveResult(
                    fromSD,
                    IndexManager.byChunkPos(fromSD.getHolder(), chunkPos)
            ));

            return result;
        }

        // TODO:: some route
        public static List<ClueObject> retrieveAllSavedData(Level level) {
            IRuntimeLocation fromSD = new FromSavedData(level);
            return new ArrayList<>(fromSD.getHolder().values());
        }
        // TODO: also (on client) some distance culling, and traverse nearby chunks and get attachments

    }

    public static class Client {
        public static List<RetrieveResult> retrieveByBlockPos(Level level, BlockPos pos) {
            return Common.retrieveByBlockPos(level, pos);
        }

        public static List<RetrieveResult> retrieveByEntity(Entity entity) {
            return Common.retrieveByEntity(entity);
        }

    }

    public static class Server {
        public static List<RetrieveResult> retrieveByBlockPos(Level level, BlockPos pos) {
            return Common.retrieveByBlockPos(level, pos);
        }

        public static List<RetrieveResult> retrieveByEntity(Entity entity) {
            return Common.retrieveByEntity(entity);
        }

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
