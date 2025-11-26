package io.github.daxigua2333.mocai_clues.footprints.statics;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.footprints.Footprint;
import io.github.daxigua2333.mocai_clues.footprints.FootprintBlockPosMap;
import io.github.daxigua2333.mocai_clues.footprints.FootprintCoordinateMap;
import io.github.daxigua2333.mocai_clues.footprints.ModFootprintRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.ArrayList;
import java.util.List;

public final class FootprintClientHelper {
    public static List<Footprint> getAllByChunk(LevelChunk chunk){
        List<Footprint> result = new ArrayList<>();
        FootprintBlockPosMap map = chunk.getData(ModFootprintRegistry.FOOTPRINT_MAP.get());
        for (BlockPos pos : map.keySet()){
            FootprintCoordinateMap cooMap = map.getExisting(pos);
            result.addAll(cooMap.values());
        }
        return result;
    }
}
