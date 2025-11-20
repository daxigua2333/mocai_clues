package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.data_attachments.statics.ClueContainerAttachmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ClueInventoryMenu extends AbstractContainerMenu {

    private final int containerRows = 3;
    public ItemStackHandler inventory;
    public BlockPos pos;


    public ClueInventoryMenu(int id, Inventory playerInv) {
        this(id, playerInv, new ItemStackHandler(27), new BlockPos(0,0,0));   // TODO: rows config
    }

    public ClueInventoryMenu(int id, Inventory playerInv, ItemStackHandler inventory, BlockPos pos) {
        super(ModMenuTypeRegistry.CLUE_INVENTORY_MENU.get(), id);
        this.inventory = inventory;
        this.pos = pos;
        int yOffset = (this.containerRows - 4) * 18;
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide && player instanceof ServerPlayer sp && isHandlerEmpty(this.inventory)) {
            ClueContainerAttachmentHelper.remove(sp.level(), this.pos);
        }
    }
    private static boolean isHandlerEmpty(ItemStackHandler handler) {
//        if (handler == null) return true;
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
