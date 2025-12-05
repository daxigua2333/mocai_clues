package io.github.daxigua2333.mocai_clues.data.server;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.DeltaType;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectHolderDeltaSyncPayload;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectMainMapSyncPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;

public class ClueObjectMainMapInSavedData extends SavedData {
    private final Map<UUID, ClueObject> map;  // !!!! use ordered map

    // ===== constructors =====
    public ClueObjectMainMapInSavedData(Map<UUID, ClueObject> map) {
        this.map = new LinkedHashMap<>(map);
    }
    public ClueObjectMainMapInSavedData() {
        this(new LinkedHashMap<>());
    }


    public static ClueObjectMainMapInSavedData getInstance(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(ClueObjectMainMapInSavedData::new, ClueObjectMainMapInSavedData::load),
                "clue_object_main_map_in_saved_data"
        );
    }


    // ===== persist ======
    private static ClueObjectMainMapInSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        ListTag list = tag.getList("ClueObjectMainMap", Tag.TAG_COMPOUND);
        Map<UUID, ClueObject> result = new LinkedHashMap<>();
        for (int i=0; i<list.size(); i++) {
            ClueObject obj = ClueObject.fromNbt(list.getCompound(i));
            result.put(obj.getId(), obj);
        }
        return new ClueObjectMainMapInSavedData(result);
    }
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (ClueObject value : map.values()) {
            list.add(value.toNbt());
        }
        tag.put("ClueObjectMainMap", list);
        return tag;
    }

    // ====== sync =====
    public static final StreamCodec<RegistryFriendlyByteBuf, ClueObjectMainMapInSavedData> STREAM_CODEC =
            StreamCodec.of(
                    (RegistryFriendlyByteBuf buf, ClueObjectMainMapInSavedData map) -> {
                        buf.writeInt(map.size());
                        for (ClueObject value : map.values()) {
                            ClueObject.STREAM_CODEC.encode(buf, value);
                        }
                    },
                    buf -> {
                        int size = buf.readInt();
                        Map<UUID, ClueObject> map = new LinkedHashMap<>(size);
                        for (int i=0; i<size; i++) {
                            ClueObject value = ClueObject.STREAM_CODEC.decode(buf);
                            map.put(value.getId(), value);
                        }
                        return new ClueObjectMainMapInSavedData(map);
                    }
            );

    private void syncDeltaToAll(DeltaType type, @Nullable UUID id, @Nullable ClueObject object) {
        // TODO: optimize, send the delta
        PacketDistributor.sendToAllPlayers(new ClueObjectHolderDeltaSyncPayload(type, id, object));
    }
    public void syncAllToOne(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ClueObjectMainMapSyncPayload(this.map));
    }


    // ===== setter =====  setDirty() sync()
    public void put(UUID id, ClueObject object) {
        this.map.put(id, object);
        this.setDirty();
        this.syncDeltaToAll(DeltaType.PUT, id, object);
    }
    public void remove(UUID id) {
        map.remove(id);
        this.setDirty();
        this.syncDeltaToAll(DeltaType.REMOVE, id, null);
    }
    public void clear() {
        map.clear();
        this.setDirty();
        this.syncDeltaToAll(DeltaType.CLEAR, null, null);
    }
    // ===== getters ======
    @Nullable
    public ClueObject getExisting(UUID id) {
        return map.get(id);
    }
    public Collection<ClueObject> values() {
        return map.values();
    }
    public int size() {
        return map.size();
    }



}
