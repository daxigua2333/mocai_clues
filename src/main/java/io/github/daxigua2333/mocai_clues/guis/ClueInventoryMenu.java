package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ClueInventoryMenu extends AbstractContainerMenu {

    private final int containerRows;

    public ClueInventoryMenu(int id, Inventory playerInv) {
        this(id, playerInv, new ItemStackHandler(27));   // TODO: rows config
    }

    public ClueInventoryMenu(int id, Inventory playerInv, ItemStackHandler inventory) {
        super(ModMenuTypeRegistry.CLUE_INVENTORY_MENU.get(), id);
        int rows = 3;
        this.containerRows = rows;
        int yOffset = (rows - 4) * 18;
        // container inventory
        for(int i = 0; i < this.containerRows; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new SlotItemHandler(inventory, j + i * 9, 8 + j * 18, 18 + i * 18));
            }
        }
        // Player inventory
        for (int i = 0; i < this.containerRows; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 103 + yOffset + i * 18));
            }
        }
        // Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 161 + yOffset));
        }

    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot)this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.containerRows * 9) {
                if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;

    }

    @Override
    public boolean stillValid(Player player) {  // TODO
        return true;
    }

}
