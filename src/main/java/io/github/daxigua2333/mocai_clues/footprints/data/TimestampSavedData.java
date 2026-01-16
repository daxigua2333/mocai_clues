package io.github.daxigua2333.mocai_clues.footprints.data;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.footprints.ModFootprintRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TimestampSavedData extends SavedData {

    private long timestamp;
    private final Map<Long, Set<Vec3>> map;

    public TimestampSavedData() {
        this(0, new HashMap<>());
    }
    public TimestampSavedData(long timestamp, Map<Long, Set<Vec3>> map) {
        this.timestamp = timestamp;
        this.map = map;
    }

    public static TimestampSavedData getInstance(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(TimestampSavedData::new, TimestampSavedData::load),
                "mocai_clue_footprint_timestamp"
        );
    }

    private static TimestampSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return new TimestampSavedData(
                tag.getLong("timestamp"),
                readNBT(tag.getCompound("TimestampIndexMap"))
        );
    }
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putLong("timestamp", timestamp);
        tag.put("TimestampIndexMap", writeNBT(map));
        return tag;
    }


    public long getTimestamp() {
        return timestamp;
    }

    /*
    * query ALL the level chunks and execute DELETE
    * which means there are *loaded* and *unloaded* chunks
    * TODO: optimize the process of level.getChunk(),getData(attach)
    * I think here we should use lazy delete for unloaded ones,
    * but idk whether it is reliable..... like generating new chunk / loading chunk from disk seems to be 2 events
    * */
    public void tick(ServerLevel level) {
        boolean daylightOn = level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT); // doDaylightCycle
        if (daylightOn) {
            timestamp++;

            AttachmentType<FootprintMainMap> type = ModFootprintRegistry.FOOTPRINT_MAIN_MAP.get();

            for (Vec3 vec : map.getOrDefault(timestamp, new HashSet<>())) {
                ChunkPos chunkPos = new ChunkPos(BlockPos.containing(vec));
                ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
                FootprintMainMap map = chunk.getData(type);
                Runnable markDirty = () -> chunk.setUnsaved(true);

                float expireRate = (float) Config.SERVER.FOOTPRINT_EXPIRE_RATE.getAsDouble();
//                float expireRate = 0.2f;
                if (!map.containsKey(vec)) continue;
                Footprint oldPrint = map.getExisting(vec);
                float newAlpha = oldPrint.alpha() - expireRate * (float) Config.SERVER.FOOTPRINT_INIT_ALPHA.getAsDouble();
                if (newAlpha <= 0 || oldPrint.createdTime() + oldPrint.lifetime() <= this.timestamp) {
                    // then only delete
                    map.remove(vec, markDirty);
                    this.deleteCurrent(vec);
                } else {
                    // delete and create

                    // update MainMap
                    Footprint newPrint = new Footprint(
                            oldPrint.x(), oldPrint.y(), oldPrint.z(), oldPrint.rotation(), oldPrint.longSide(), oldPrint.shortSide(),
                            newAlpha,
                            oldPrint.createdTime(), oldPrint.lifetime(),
                            oldPrint.ownerUUID()
                    );
                    map.remove(vec, markDirty);
                    map.put(vec, newPrint, markDirty);
                    // update IndexMap
                    this.addToSetWithOffset(getExpireTimeOffset(oldPrint.lifetime()), vec);
                    this.deleteCurrent(vec);
                }
                // dirty
//                chunk.setData(type, map);
                chunk.syncData(type);
            }

            this.map.remove(timestamp);  // true delete part

            // dirty
            setDirty();
        }
    }

    // helper
    public static long getExpireTimeOffset(int lifetime){
        float expireRate = (float) Config.SERVER.FOOTPRINT_EXPIRE_RATE.getAsDouble();
//        float expireRate = 0.2f;
        return (long) Math.round(lifetime * expireRate);  // round, and I think there should be no problem
    }

    private Set<Vec3> getOrCreate(long timestamp) {
        if (map.containsKey(timestamp)) {
            return map.get(timestamp);
        } else {
            Set<Vec3> set = new HashSet<>();
            map.put(timestamp, set);
            return set;
        }
    }
    public void addToSetWithOffset(Long expireTimeOffset, Vec3 vec) {
        long newStamp = this.timestamp + expireTimeOffset;
        Set<Vec3> set = this.getOrCreate(newStamp);
        set.add(vec);
    }
    public void deleteCurrent(Vec3 vec) {}  // delete part is in tick(), so this is empty

    private static CompoundTag writeNBT(Map<Long, Set<Vec3>> map) {
        CompoundTag root = new CompoundTag();

        for (Map.Entry<Long, Set<Vec3>> entry : map.entrySet()) {
            long key = entry.getKey();
            Set<Vec3> vecSet = entry.getValue();

            ListTag vecList = new ListTag();
            for (Vec3 vec : vecSet) {
                CompoundTag vecTag = new CompoundTag();
                vecTag.putDouble("x", vec.x);
                vecTag.putDouble("y", vec.y);
                vecTag.putDouble("z", vec.z);
                vecList.add(vecTag);
            }

            root.put(Long.toString(key), vecList);
        }

        return root;
    }

    private static Map<Long, Set<Vec3>> readNBT(CompoundTag root) {
        Map<Long, Set<Vec3>> result = new HashMap<>();

        for (String keyStr : root.getAllKeys()) {
            ListTag vecList = root.getList(keyStr, Tag.TAG_COMPOUND);
            Set<Vec3> vecSet = new HashSet<>();

            for (int i = 0; i < vecList.size(); i++) {
                CompoundTag vecTag = vecList.getCompound(i);
                double x = vecTag.getDouble("x");
                double y = vecTag.getDouble("y");
                double z = vecTag.getDouble("z");
                vecSet.add(new Vec3(x, y, z));
            }

            long key = Long.parseLong(keyStr);
            result.put(key, vecSet);
        }

        return result;
    }
}
