package io.github.daxigua2333.cmagic_clue.data_attachments;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClueContainerMap implements INBTSerializable<CompoundTag> {
    private final Map<BlockPos, ClueContainer> map = new HashMap<>();

    public ClueContainerMap() {}

//    public ItemStackHandler getOrCreate(BlockPos pos, Supplier<ItemStackHandler> creator) {
//        return map.computeIfAbsent(pos.immutable(), p -> creator.get());
//    }
//
    public ClueContainer getExisting(BlockPos pos) {
        return map.get(pos);
    }
    public boolean containsKey(BlockPos pos){
        return map.containsKey(pos);
    }
    public List<BlockPos> getKeys() {
        return new ArrayList<>(map.keySet());
    }

    // TODO: but I'm lazy (chunk/IAttachmentHolder)
    public void remove(BlockPos pos, LevelChunk chunk){
        chunk.setUnsaved(true);
        map.remove(pos);
    }
    public void put(BlockPos pos, ClueContainer container, LevelChunk chunk){
        chunk.setUnsaved(true);
        map.put(pos, container);
    }


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, ClueContainer> e : map.entrySet()) {
            BlockPos p = e.getKey();
            ClueContainer container = e.getValue();
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", p.getX());
            entry.putInt("y", p.getY());
            entry.putInt("z", p.getZ());
            entry.put("inv", container.serializeNBT(provider));
            list.add(entry);
        }
        root.put("clues", list);
        return root;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        map.clear();
        ListTag list = nbt.getList("clues", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int x = entry.getInt("x");
            int y = entry.getInt("y");
            int z = entry.getInt("z");
            BlockPos pos = new BlockPos(x, y, z);
            CompoundTag invTag = entry.getCompound("inv");
            ClueContainer container = new ClueContainer();
            container.deserializeNBT(provider, invTag);
            map.put(pos, container);
        }

    }
}
