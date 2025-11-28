package io.github.daxigua2333.mocai_clues.footprints.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class FootprintAttachedPosIndexMap {
    private final Map<BlockPos, Set<Vec3>> map;

    // codec
    private static final Codec<Set<Vec3>> VEC3_SET_CODEC =
        Vec3.CODEC.listOf().xmap(
            list -> new HashSet<>(list),
            set -> new ArrayList<>(set)
        );
    private static final Codec<Pair<BlockPos, Set<Vec3>>> ENTRY_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(Pair::getFirst),
            VEC3_SET_CODEC.fieldOf("set").forGetter(Pair::getSecond)
        ).apply(instance, Pair::of)
    );
    private static final Codec<Map<BlockPos, Set<Vec3>>> MAP_CODEC =
        ENTRY_CODEC.listOf().xmap(
            list -> {
                Map<BlockPos, Set<Vec3>> map = new HashMap<>();
                for (Pair<BlockPos, Set<Vec3>> pair : list) {
                    map.put(pair.getFirst(), pair.getSecond());
                }
                return map;
            },
            map -> map.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .toList()
        );
    public static final Codec<FootprintAttachedPosIndexMap> CODEC = MAP_CODEC.xmap(FootprintAttachedPosIndexMap::new, FootprintAttachedPosIndexMap::getMap);

    public FootprintAttachedPosIndexMap() {
        this(new HashMap<>());
    }
    public FootprintAttachedPosIndexMap(Map<BlockPos, Set<Vec3>> map) {
        this.map = new HashMap<>(map);
    }
    private Map<BlockPos, Set<Vec3>> getMap() {
        return map;
    }

    // getter setter
    public Boolean containsKey(BlockPos pos) {
        return map.containsKey(pos);
    }
    public Set<Vec3> getExisting(BlockPos pos) {
        return map.get(pos);
    }
    public Set<BlockPos> keySet(){
        return map.keySet();
    }
    public Set<Vec3> getOrCreate(BlockPos pos) {
        if (map.containsKey(pos)) {
            return map.get(pos);
        } else {
            Set<Vec3> set = new HashSet<>();
            map.put(pos, set);
//            markDirty.run();  // TODO: idk whether mark dirty or just create default each #get, which one is faster
            return set;
        }
    }
    public void addToSet(BlockPos pos, Vec3 vec3, Runnable markDirty) {
        Set<Vec3> set = this.getOrCreate(pos);
        set.add(vec3);
        markDirty.run();
    }

    public void remove(BlockPos pos, Runnable markDirty) {
        map.remove(pos);
        markDirty.run();
    }
}
