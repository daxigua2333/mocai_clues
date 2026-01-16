package io.github.daxigua2333.mocai_clues.data.server;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.networks.ClueObjectHolderDeltaPayload;
import io.github.daxigua2333.mocai_clues.data.networks.ClueObjectHolderFullPayload;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Function;

// Server-only, attached to overworld or per-dimension
public class ClueObjectHolderInSavedData extends SavedData {
    public static final String ID = "clue_object_holder";

    // The actual registry/map
    private final ObjectHolder<ClueObject> holder;

    // --- construction -------------------------------------------------------

    // Empty (new) data
    public static ClueObjectHolderInSavedData create() {
        // Start empty; you can also pass some initial values
        return new ClueObjectHolderInSavedData(
            new ObjectHolder<>(
                ClueObject::getId,
                ClueObject.CODEC,
                ClueObject.STREAM_CODEC
            )
        );
    }

    private ClueObjectHolderInSavedData(ObjectHolder<ClueObject> holder) {
        this.holder = holder;
    }

    public static final Codec<ObjectHolder<ClueObject>> CLUE_HOLDER_CODEC =
        ObjectHolder.codec(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC)
            .xmap(
                holder -> { ServerIndexManager.savedDataEnsure(holder); return holder; }, // decode
                Function.identity()
            );

    // Load from NBT. (persistence)
    public static ClueObjectHolderInSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.contains("ClueObjectHolder")) {
            return create();
        }

        // If your ClueObject.CODEC is registry-free, NbtOps is fine.
        // If it uses registries (items, blocks, etc.), use RegistryOps instead (see note below).
        var dataResult = CLUE_HOLDER_CODEC
            .parse(NbtOps.INSTANCE, tag.get("ClueObjectHolder"));

        ObjectHolder<ClueObject> holder = dataResult
            .resultOrPartial(System.err::println)
            .orElseGet(() -> new ObjectHolder<>(ClueObject::getId, ClueObject.CODEC, ClueObject.STREAM_CODEC));

        return new ClueObjectHolderInSavedData(holder);
    }

    // Save to NBT. (persistence)
    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        var dataResult = CLUE_HOLDER_CODEC
            .encodeStart(NbtOps.INSTANCE, this.holder);

        dataResult
            .resultOrPartial(System.err::println)
            .ifPresent(encoded -> tag.put("ClueObjectHolder", (Tag) encoded));

        return tag;
    }

    // --- helper to get the instance from a ServerLevel ----------------------

    // only allow access to overworld SD
    // use anyLevel.getServer() to get MinecraftServer
    public static ClueObjectHolderInSavedData getInstance(MinecraftServer server) {
        return ClueObjectHolderInSavedData.getInstance(server.overworld());
    }
    private static ClueObjectHolderInSavedData getInstance(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(
            new SavedData.Factory<>(ClueObjectHolderInSavedData::create, ClueObjectHolderInSavedData::load),
            ID
        );
    }

    public ObjectHolder<ClueObject> holder() {
        return holder;
    }

    // ====== sync ========
    public void syncAllTo(ServerPlayer player) {
//    public static void syncAllTo(ServerPlayer player) {
//        ServerLevel level = player.serverLevel();
//        ClueObjectHolderInSavedData data = ClueObjectHolderInSavedData.getInstance(level);
        PacketDistributor.sendToPlayer(
            player,
            new ClueObjectHolderFullPayload(this.holder)
        );
    }
    public void syncDeltaToAll() {
//    public static void syncDeltaToAll(ServerLevel level) {
//        var SD = ClueObjectHolderInSavedData.getInstance(level);

        PacketDistributor.sendToAllPlayers(new ClueObjectHolderDeltaPayload(
                this.holder.getDeltaPayload()
        ));
        this.holder.resetChangeTracking();  // consume the changes
    }


    // Convenience API so all mutations go through here
    public void put(ClueObject obj) {
        holder.put(obj);
        setDirty();
    }

    public void remove(UUID id) {
        holder.remove(id);
        setDirty();
    }

    public void clear() {
        holder.clear();
        setDirty();
    }

    /** if deep mutable objects are mutated, don't forget to call this */
    @Override
    public void setDirty() {
        super.setDirty();
//        holder.markDirty();
        if (holder.hasChanges()) {
            syncDeltaToAll();
        }
    }
}
