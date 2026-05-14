package io.github.daxigua2333.cmagic_clue.footprints.statics;

import io.github.daxigua2333.cmagic_clue.footprints.data.Footprint;
import io.github.daxigua2333.cmagic_clue.footprints.data.FootprintMainMap;
import io.github.daxigua2333.cmagic_clue.footprints.ModFootprintRegistry;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Collection;

public final class FootprintClientHelper {
    public static Collection<Footprint> getAllByChunk(LevelChunk chunk){
        FootprintMainMap map = chunk.getData(ModFootprintRegistry.FOOTPRINT_MAIN_MAP.get());
        return map.values();
    }
}
