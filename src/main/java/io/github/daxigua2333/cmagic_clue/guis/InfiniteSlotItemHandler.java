package io.github.daxigua2333.cmagic_clue.guis;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class InfiniteSlotItemHandler extends SlotItemHandler {
    private boolean isCreative;
    public final void setIsCreative(final boolean isCreative) {this.isCreative = isCreative;}

    public InfiniteSlotItemHandler(ItemStackHandler handler, int index, int x, int y, boolean isCreative) {
        super(handler, index, x, y);
        this.isCreative = isCreative;
    }

    /**
     * Do NOT allow players except creative to put anything in these slots.
     * They are only templates.
     */
    @Override
    public boolean mayPlace(ItemStack stack) {
        if (isCreative) {
            return super.mayPlace(stack);
        }else {
            return false;
        }
    }

    /**
     * When the menu wants to take items from this slot, we
     * just return a COPY and do NOT modify the handler.
     */
    @Override
    public ItemStack remove(int amount) {
        ItemStack template = getItem();
        if (template.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        // Always give a full stack or up to max stack size:
        ItemStack result = template.copy();
//        int max = template.getMaxStackSize();
//        result.setCount(Math.min(amount, max));
        result.setCount(1);
        return result;
    }

    /**
     * Optional: don't do anything special on take – the handler
     * has not changed anyway.
     */
    @Override
    public void onTake(Player player, ItemStack stack) {
        // No shrinking, no changes to the template
        super.onTake(player, stack);
    }
}
