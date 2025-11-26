package io.github.daxigua2333.mocai_clues.footprints.statics;

import io.github.daxigua2333.mocai_clues.footprints.data.Footprint;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintAttachedPosIndexMap;
import io.github.daxigua2333.mocai_clues.footprints.data.FootprintMainMap;
import io.github.daxigua2333.mocai_clues.footprints.ModFootprintRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;

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

        createMainFootprint(chunk, x, y, z, rotation, longSide, shortSide, alpha);
        createAttachedPosIndex(chunk, blockBelow, x, y, z);
        // life cycle part(index)

    }

    private static void createMainFootprint(LevelChunk chunk, double x, double y, double z, float rotation, float longSide, float shortSide, float alpha) {
        AttachmentType<FootprintMainMap> type = ModFootprintRegistry.FOOTPRINT_MAIN_MAP.get();
        FootprintMainMap map = chunk.getData(type);

        Footprint footprint = new Footprint(x, y, z, rotation, longSide, shortSide, alpha);
        map.put(new Vec3(x, y, z), footprint, () -> chunk.setUnsaved(true));

        chunk.setData(type, map);
    }

    private static void createAttachedPosIndex(LevelChunk chunk, BlockPos pos, double x, double y, double z) {
        AttachmentType<FootprintAttachedPosIndexMap> type = ModFootprintRegistry.FOOTPRINT_ATTACHED_POS_INDEX_MAP.get();
        FootprintAttachedPosIndexMap map = chunk.getData(type);

        map.addToSet(pos, new Vec3(x,y,z), () -> chunk.setUnsaved(true));

        chunk.setData(type, map);
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
