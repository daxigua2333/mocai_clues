package io.github.daxigua2333.cmagic_clue.guis.whitelist;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class GhostSlot extends SlotItemHandler {
    public GhostSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        // Prevent taking the ghost item out using vanilla logic
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Prevent vanilla from inserting (which would shrink the carried stack)
        return false;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
