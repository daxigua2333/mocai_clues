package io.github.daxigua2333.mocai_clues.footprints.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class FootprintMainMap {
    private final Map<Vec3, Footprint> map;

    // codec
    private static final Codec<Pair<Vec3, Footprint>> ENTRY_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Vec3.CODEC.fieldOf("vec3").forGetter(Pair::getFirst),
            Footprint.CODEC.fieldOf("data").forGetter(Pair::getSecond)
        ).apply(instance, Pair::of)
    );
    private static final Codec<Map<Vec3, Footprint>> MAP_CODEC =
        ENTRY_CODEC.listOf().xmap(
            list -> {
                Map<Vec3, Footprint> map = new HashMap<>();
                for (Pair<Vec3, Footprint> pair : list) {
                    map.put(pair.getFirst(), pair.getSecond());
                }
                return map;
            },
            map -> map.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .toList()
        );
//    private static final Codec<Map<Vec3, Footprint>> MAP_CODEC = Codec.unboundedMap(Vec3.CODEC, Footprint.CODEC);
    public static final Codec<FootprintMainMap> CODEC = MAP_CODEC.xmap(FootprintMainMap::new, FootprintMainMap::getMap);
    // stream codec
    public static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC =
            StreamCodec.of(
                // encoder
                (buf, vec) -> {
                    buf.writeDouble(vec.x());
                    buf.writeDouble(vec.y());
                    buf.writeDouble(vec.z());
                },
                // decoder
                buf -> new Vec3(
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble()
                )
            );
    private static final StreamCodec<ByteBuf, Map<Vec3, Footprint>> MAP_STREAM_CODEC = ByteBufCodecs.map(
            HashMap::new, // Constructs a map with the specified capacity
//            Vec3.STREAM_CODEC,
            VEC3_STREAM_CODEC,
            Footprint.STREAM_CODEC,
            256 // The map can only have up to 256 elements
    );
    public static final StreamCodec<ByteBuf, FootprintMainMap> STREAM_CODEC = MAP_STREAM_CODEC.map(FootprintMainMap::new, FootprintMainMap::getMap);

    public FootprintMainMap() {
        this(new HashMap<>());
    }

    public FootprintMainMap(Map<Vec3, Footprint> map) {
//        this.map = map;  TODO
        this.map = new HashMap<>(map);
    }
    private Map<Vec3, Footprint> getMap() {
        return map;
    }

    // getter setter
    public Boolean containsKey(Vec3 pos) {
        return map.containsKey(pos);
    }
    public Set<Vec3> keySet() {
        return map.keySet();
    }
    public Footprint getExisting(Vec3 pos) {
        return map.get(pos);
    }
    public Collection<Footprint> values() {
        return map.values();
    }
    public void put(Vec3 pos, Footprint footprint, Runnable markDirty) {
        map.put(pos, footprint);
        markDirty.run();
    }
    public void remove(Vec3 pos, Runnable markDirty) {
        map.remove(pos);
        markDirty.run();
    }
}
