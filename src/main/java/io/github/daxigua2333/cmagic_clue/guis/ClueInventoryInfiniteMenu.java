package io.github.daxigua2333.cmagic_clue.guis;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ClueInventoryInfiniteMenu extends ClueInventoryMenu{

    public ClueInventoryInfiniteMenu(int id, Inventory playerInv) {
        super(id, playerInv);
    }
    public ClueInventoryInfiniteMenu(int id, Inventory playerInv, ItemStackHandler inventory, BlockPos pos, boolean creative) {
        super(id, playerInv, inventory, pos, creative);
    }

    @Override
    protected void makeContainerInventory(ItemStackHandler inventory) {
        for(int i = 0; i < this.containerRows; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new InfiniteSlotItemHandler(inventory, j + i * 9, 8 + j * 18, 18 + i * 18, isCreative));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);

        // infinite slot logic
        if (slot instanceof InfiniteSlotItemHandler) {
            ItemStack template = slot.getItem();
            if (template.isEmpty()) {
                return ItemStack.EMPTY;
            }

            if (!this.moveOneFromInfiniteTo(template, this.containerRows * 9, this.slots.size())) {
                return ItemStack.EMPTY; // no space
            }

            slot.setChanged();
            return ItemStack.EMPTY;
        }

        // vanilla chest logic
        return super.quickMoveStack(player, index);
//        return ItemStack.EMPTY;
    }

    private boolean moveOneFromInfiniteTo(ItemStack template, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; ++i) {
            Slot searchingSlot = this.slots.get(i);
            ItemStack searchingStack = searchingSlot.getItem();
            int count = searchingStack.getCount();
            if (ItemStack.isSameItemSameComponents(template, searchingStack) && count < searchingStack.getMaxStackSize()) {
                searchingStack.setCount(count + 1);
                searchingSlot.setChanged();
                return true;
            }
        }
        for (int i = startIndex; i < endIndex; ++i) {
            Slot searchingSlot = this.slots.get(i);
            if (searchingSlot.getItem().isEmpty() && searchingSlot.mayPlace(template)) {
                ItemStack ret = template.copy();
                ret.setCount(1);
                searchingSlot.setByPlayer(ret);
                searchingSlot.setChanged();
                return true;
            }
        }
        return false;
    }
}
