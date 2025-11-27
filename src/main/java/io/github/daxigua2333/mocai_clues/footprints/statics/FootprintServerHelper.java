package io.github.daxigua2333.mocai_clues.footprints.statics;

import io.github.daxigua2333.mocai_clues.Configs.Config;
import io.github.daxigua2333.mocai_clues.footprints.data.Footprint;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintAttachedPosIndexMap;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintMainMap;
import io.github.daxigua2333.mocai_clues.footprints.ModFootprintRegistry;
import io.github.daxigua2333.mocai_clues.footprints.data.TimestampSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FootprintServerHelper {

    // TODO: network performance
    // AttachmentHolder #getData #setData #removeData #sync

//    public static Boolean containsKey(Level level, BlockPos pos) {
//        LevelChunk chunk = level.getChunkAt(pos);
//        FootprintBlockPosMap map = chunk.getData(ModFootprintRegistry.FOOTPRINT_MAP.get());
//        return map.containsKey(pos);
//    }
//
//    @Nullable
//    public static Footprint getExisting(Level level, BlockPos pos) {
//        LevelChunk chunk = level.getChunkAt(pos);
//        FootprintBlockPosMap map = chunk.getData(ModFootprintRegistry.FOOTPRINT_MAP.get());
//        if (map.containsKey(pos)) {
//            return map.getExisting(pos);
//        } else {
//            return null;
//        }
//    }

    // ==== CREATE ====
    // insert into main DB map, and update all index maps
    public static void create(Level level, BlockPos blockBelow, double x, double y, double z, float rotation, float longSide, float shortSide, float alpha) {
        LevelChunk chunk = level.getChunkAt(blockBelow);

        int lifetime = createLifetime(level.getBlockState(blockBelow));
        createMainFootprint(chunk, x, y, z, rotation, longSide, shortSide, alpha, lifetime);
        createAttachedPosIndex(chunk, blockBelow, x, y, z);
        createTimestampIndex((ServerLevel) level, TimestampSavedData.getExpireTimeOffset(lifetime), new Vec3(x,y,z));
    }

    private static int createLifetime(BlockState blockState) {
        // TODO: hardness
        return Config.SERVER.FOOTPRINT_LIFETIME.getAsInt();
    }

    private static void createMainFootprint(LevelChunk chunk, double x, double y, double z, float rotation, float longSide, float shortSide, float alpha, int lifetime) {
        AttachmentType<FootprintMainMap> type = ModFootprintRegistry.FOOTPRINT_MAIN_MAP.get();
        FootprintMainMap map = chunk.getData(type);

        Footprint footprint = new Footprint(x, y, z, rotation, longSide, shortSide, alpha, lifetime);
        map.put(new Vec3(x, y, z), footprint, () -> chunk.setUnsaved(true));

        chunk.setData(type, map);
    }

    private static void createAttachedPosIndex(LevelChunk chunk, BlockPos pos, double x, double y, double z) {
        AttachmentType<FootprintAttachedPosIndexMap> type = ModFootprintRegistry.FOOTPRINT_ATTACHED_POS_INDEX_MAP.get();
        FootprintAttachedPosIndexMap map = chunk.getData(type);

        map.addToSet(pos, new Vec3(x,y,z), () -> chunk.setUnsaved(true));

        chunk.setData(type, map);
    }

    private static void createTimestampIndex(ServerLevel level, long expireOffset, Vec3 vec) {
        TimestampSavedData timeData = TimestampSavedData.getInstance(level);
        timeData.addToSetWithOffset(expireOffset, vec);
    }


    // ==== DELETE =====
    // not necessarily remove all the index that point to the deleted value
    // thus remember, removing by certain index may get null
    public static void deleteByBlockPos(Level level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        AttachmentType<FootprintAttachedPosIndexMap> type = ModFootprintRegistry.FOOTPRINT_ATTACHED_POS_INDEX_MAP.get();
        FootprintAttachedPosIndexMap map = chunk.getData(type);

        AttachmentType<FootprintMainMap> mainType = ModFootprintRegistry.FOOTPRINT_MAIN_MAP.get();
        FootprintMainMap mainMap = chunk.getData(mainType);
        for (Vec3 coo : map.getOrCreate(pos)) {
            mainMap.remove(coo, () -> chunk.setUnsaved(true));
        }
        map.remove(pos, () -> chunk.setUnsaved(true));

        chunk.setData(mainType, mainMap);
        chunk.setData(type, map);
    }

    public static void deleteByCoordinate(LevelChunk chunk, Vec3 coordinate) {
        AttachmentType<FootprintMainMap> type = ModFootprintRegistry.FOOTPRINT_MAIN_MAP.get();
        FootprintMainMap map = chunk.getData(type);

        map.remove(coordinate, () -> chunk.setUnsaved(true));
        chunk.setData(type, map);
    }

    public static void deleteByCoordinate(Level level, Vec3 coordinate) {

    }

}
