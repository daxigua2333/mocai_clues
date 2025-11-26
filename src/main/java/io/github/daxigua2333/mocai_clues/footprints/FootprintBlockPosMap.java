package io.github.daxigua2333.mocai_clues.footprints;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FootprintBlockPosMap {
    private final Map<BlockPos, FootprintCoordinateMap> map;

    // codec
    private static final Codec<Pair<BlockPos, FootprintCoordinateMap>> ENTRY_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Pair::getFirst),
            FootprintCoordinateMap.CODEC.fieldOf("map").forGetter(Pair::getSecond)
        ).apply(instance, Pair::of)
    );
    private static final Codec<Map<BlockPos, FootprintCoordinateMap>> MAP_CODEC =
        ENTRY_CODEC.listOf().xmap(
            list -> {
                Map<BlockPos, FootprintCoordinateMap> map = new HashMap<>();
                for (Pair<BlockPos, FootprintCoordinateMap> pair : list) {
                    map.put(pair.getFirst(), pair.getSecond());
                }
                return map;
            },
            map -> map.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .toList()
        );
//    private static final Codec<Map<BlockPos, FootprintCoordinateMap>> MAP_CODEC = Codec.unboundedMap(BlockPos.CODEC, FootprintCoordinateMap.CODEC);
    public static final Codec<FootprintBlockPosMap> CODEC = MAP_CODEC.xmap(FootprintBlockPosMap::new, FootprintBlockPosMap::getMap);
    // stream codec
    private static final StreamCodec<ByteBuf, Map<BlockPos, FootprintCoordinateMap>> MAP_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new, // Constructs a map with the specified capacity
            BlockPos.STREAM_CODEC,
            FootprintCoordinateMap.STREAM_CODEC
//            256 // The map can only have up to 256 elements
    );
    public static final StreamCodec<ByteBuf, FootprintBlockPosMap> STREAM_CODEC = MAP_STREAM_CODEC.map(FootprintBlockPosMap::new, FootprintBlockPosMap::getMap);

    public FootprintBlockPosMap() {
        this(new HashMap<>());
    }
    public FootprintBlockPosMap(Map<BlockPos, FootprintCoordinateMap> map) {
//        this.map = map;  TODO: performance
        this.map = new HashMap<>(map);
    }
    private Map<BlockPos, FootprintCoordinateMap> getMap() {
        return map;
    }

    // getter setter
    public Boolean containsKey(BlockPos pos) {
        return map.containsKey(pos);
    }
    public FootprintCoordinateMap getExisting(BlockPos pos) {
        return map.get(pos);
    }
    public Set<BlockPos> keySet(){
        return map.keySet();
    }
    public FootprintCoordinateMap getOrCreate(BlockPos pos) {
        // TODO: idk whether mark dirty or just create default each #get, which one is faster
        if (map.containsKey(pos)) {
            return map.get(pos);
        } else {
            FootprintCoordinateMap n = new FootprintCoordinateMap();
            map.put(pos, n);
            return n;
        }
    }
    public FootprintCoordinateMap getOrCreate(BlockPos pos, FootprintCoordinateMap coordinateMap) {
        if (map.containsKey(pos)) {
            return map.get(pos);
        } else {
            map.put(pos, coordinateMap);
            return coordinateMap;
        }
    }

    // don't put, use get and then put to update
//    public void put(BlockPos pos, FootprintCoordinateMap footprint, LevelChunk chunk) {
//        map.put(pos, footprint);
//        chunk.setUnsaved(true);
//    }
    public void remove(BlockPos pos, Runnable markDirty) {
        map.remove(pos);
        markDirty.run();
    }
}
