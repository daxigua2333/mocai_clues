package io.github.daxigua2333.mocai_clues.footprints.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class FootprintMainMap {
    private final Map<Vec3, Footprint> map;

    // Server-side dirty tracking (not serialized, not sent in full sync)
    private final Map<Vec3, Footprint> dirtyUpserts = new HashMap<>();
    private final Set<Vec3> dirtyRemovals = new HashSet<>();

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
            Footprint.STREAM_CODEC
//            256 // The map can only have up to 256 elements
    );
    public static final StreamCodec<ByteBuf, FootprintMainMap> STREAM_CODEC = MAP_STREAM_CODEC.map(FootprintMainMap::new, FootprintMainMap::getMap);

    public FootprintMainMap() {
        this(new HashMap<>());
    }

    public FootprintMainMap(Map<Vec3, Footprint> map) {
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
    public @Nullable Footprint getExisting(Vec3 pos) {
        return map.get(pos);
    }
    public Collection<Footprint> values() {
        return map.values();
    }
    public void put(Vec3 pos, Footprint footprint, Runnable markDirty) {
        map.put(pos, footprint);
        dirtyUpserts.put(pos, footprint);
        dirtyRemovals.remove(pos);
        markDirty.run();
    }
    public void remove(Vec3 pos, Runnable markDirty) {
        if (map.remove(pos) != null) {
            dirtyRemovals.add(pos);
            dirtyUpserts.remove(pos);
            markDirty.run();
        }
    }
    public void clear(Runnable markDirty) {
        dirtyRemovals.addAll(map.keySet());
        for (Vec3 vec : map.keySet()) {
            dirtyUpserts.remove(vec);
        }
        map.clear();
        markDirty.run();
    }


    /** CLIENT: apply an update from the network – no dirty tracking, no markDirty */
    public void applyUpdateFromNetwork(Vec3 pos, Footprint footprint) {
        map.put(pos, footprint);
    }
    /** CLIENT: apply a removal from the network – no dirty tracking, no markDirty */
    public void applyRemovalFromNetwork(Vec3 pos) {
        map.remove(pos);
    }

    // Called by sync handler (server side)
    public Map<Vec3, Footprint> consumeDirtyUpserts() {
        var copy = new HashMap<>(dirtyUpserts);
        dirtyUpserts.clear();
        return copy;
    }
    public Set<Vec3> consumeDirtyRemovals() {
        var copy = new HashSet<>(dirtyRemovals);
        dirtyRemovals.clear();
        return copy;
    }

    public void clearDeltas() {
        dirtyUpserts.clear();
        dirtyRemovals.clear();
    }

    public boolean hasDelta() {
        return !dirtyUpserts.isEmpty() || !dirtyRemovals.isEmpty();
    }

}
