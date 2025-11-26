package io.github.daxigua2333.mocai_clues.footprints.statics;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.footprints.Footprint;
import io.github.daxigua2333.mocai_clues.footprints.FootprintBlockPosMap;
import io.github.daxigua2333.mocai_clues.footprints.FootprintCoordinateMap;
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

    public static void create(Level level, BlockPos blockBelow, double x, double y, double z, float rotation, float longSide, float shortSide, float alpha) {
        AttachmentType<FootprintBlockPosMap> type = ModFootprintRegistry.FOOTPRINT_MAP.get();
        LevelChunk chunk = level.getChunkAt(blockBelow);
        FootprintBlockPosMap map = chunk.getData(type);
        FootprintCoordinateMap coordinateMap = map.getOrCreate(blockBelow);

        Footprint footprint = new Footprint(x, y, z, rotation, longSide, shortSide, alpha);
        Runnable markDirty = () -> chunk.setUnsaved(true);
        coordinateMap.put(new Vec3(x, y, z), footprint, markDirty);

        chunk.setData(type, map);

        // life cycle part(index)


    }

    public static void deleteByBlockPos(Level level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        AttachmentType<FootprintBlockPosMap> type = ModFootprintRegistry.FOOTPRINT_MAP.get();
        FootprintBlockPosMap map = chunk.getData(type);
        map.remove(pos, () -> chunk.setUnsaved(true));
        chunk.setData(type, map);
    }

    public static void deleteByCoordinate(Level level, Vec3 coordinate) {

    }
}
