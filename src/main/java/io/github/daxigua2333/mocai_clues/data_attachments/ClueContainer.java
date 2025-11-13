package io.github.daxigua2333.mocai_clues.data_attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ClueContainer implements INBTSerializable<CompoundTag> {
    private IAttachmentHolder holder;
    private final ItemStackHandler inv;
    private String name = "";

//    public ClueContainer(int slots){
//        this.holder = null;
//        this.inv = new ItemStackHandler(slots) {
//            @Override
//            protected void onContentsChanged(int slot) {
//                super.onContentsChanged(slot);
//                markDirty();
//            }
//        };
//    }

    public ClueContainer(){
        this.holder = null;
        this.inv = new ItemStackHandler(27) {  // TODO: config rows...
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                markDirty();
            }
        };
    }


    public ItemStackHandler getInv(IAttachmentHolder holder) {
        this.holder = holder;  // set the dirty mark
        return inv;
    }
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name == null ? "" : name;
        markDirty();
    }

    private void markDirty() {
        if (holder == null) return;  // no holder -> no marking; chunk wrapper(?) handles marking
        if (holder instanceof BlockEntity be) {
            be.setChanged();
        } else if (holder instanceof LevelChunk chunk) {
            chunk.setUnsaved(true);
        } else if (holder instanceof Level level) {
            // level attachments are usually SavedData — skip here
        }
        // if holder is a BlockEntity, chunk, etc. we mark accordingly; other holder types probably don't need 'dirty'
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
//        markDirty();
        CompoundTag tag = new CompoundTag();
        tag.put("inv", inv.serializeNBT(provider));
        tag.putString("name", name);
        // reserved fields
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        ////////////// care about version update and new fields
        if (nbt.contains("inv")) {
            inv.deserializeNBT(provider, nbt.getCompound("inv"));
        }
        if (nbt.contains("name", Tag.TAG_STRING)) {
            name = nbt.getString("name");
        } else name = "";
        // reserved read ignored for now
    }

//    // helper: create from NBT and bind to inv (used for chunk-backed virtual attachments)
//    public static ClueContainerAttachment fromNBTForHolder(IAttachmentHolder holder, CompoundTag nbt, IntSupplier slotProvider) {
//        ClueContainerAttachment att = new ClueContainerAttachment(holder, slotProvider);
//        att.deserializeNBT(nbt);
//        return att;
//    }

//    // helper: create a tag snapshot (for sync or chunk map)
//    public CompoundTag toTagSnapshot() {
//        return serializeNBT();
//    }
}
