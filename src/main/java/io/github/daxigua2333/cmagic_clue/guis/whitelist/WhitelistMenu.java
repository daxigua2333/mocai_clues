package io.github.daxigua2333.cmagic_clue.guis.whitelist;

import io.github.daxigua2333.cmagic_clue.guis.ModMenuTypeRegistry;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class WhitelistMenu extends AbstractContainerMenu {
    // slot index inside THIS menu
    private static final int GHOST_SLOT_INDEX = 0;

    private final ItemStackHandler filterHandler;

    public ItemStack getItemStack() {
        return filterHandler.getStackInSlot(0);
    }

//    @Nullable
//    private Consumer<ItemStack> onClose;
//
//    public void setOnClose(Consumer<ItemStack> onClose) {
//        this.onClose = onClose;
//    }

    // Client constructor (opened via buffer)
//    public WhitelistMenu(int containerId, Inventory playerInv, RegistryFriendlyByteBuf buf) {
//        this(containerId, playerInv, new ItemStackHandler(1));
//        ItemStack init = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
//        filterHandler.setStackInSlot(0, init);
//    }
    public WhitelistMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, new ItemStackHandler(1));
    }

    // Server (and also client) “real” constructor
    public WhitelistMenu(int containerId, Inventory playerInv, ItemStackHandler filterHandler) {
        super(ModMenuTypeRegistry.WHITELIST_MENU.get(), containerId);
        this.filterHandler = filterHandler;

        // 1 ghost slot
//        this.addSlot(new GhostSlot(filterHandler, 0, 80, 35));
        this.addSlot(new GhostSlot(filterHandler, 0, 80, 20));

        // player inventory
//        addPlayerInventory(playerInv, 8, 84);
//        addPlayerHotbar(playerInv, 8, 142);
//        addPlayerInventory(playerInv, 8, 84);
        addPlayerInventory(playerInv, 8, 51);
        addPlayerHotbar(playerInv, 8, 109);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Let vanilla manage drag logic state; we only special-case the ghost slot on normal clicks.
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);

            if (slot instanceof GhostSlot) {
                // Handle common interactions that should set/clear the filter
                if (clickType == ClickType.PICKUP) {
                    ItemStack carried = this.getCarried(); // what the player has on cursor

                    if (carried.isEmpty()) {
                        slot.set(ItemStack.EMPTY); // clear filter
                    } else {
                        ItemStack ghost = carried.copy();
                        ghost.setCount(1);
                        slot.set(ghost); // set filter to a copy
                    }
                    return; // IMPORTANT: don't run vanilla item-moving logic
                }

                // Optional: number key on the slot sets from hotbar without swapping
                if (clickType == ClickType.SWAP) {
                    ItemStack hotbar = player.getInventory().getItem(button);
                    slot.set(hotbar.isEmpty() ? ItemStack.EMPTY : hotbar.copyWithCount(1));
                    return;
                }

                // For other click types, ignore or pass through carefully.
                // Usually safest to just return to avoid vanilla moving items:
                return;
            }
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // client side onClose
        if (player.level().isClientSide()) {
            ItemStack stack = filterHandler.getStackInSlot(0);
//            onClose.accept(stack);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // No real inventory here; disable shift-click transfers
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // or the normal stillValid(ContainerLevelAccess, ...) if bound to a block
    }

    private void addPlayerInventory(Inventory inv, int leftCol, int topRow) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = leftCol + col * 18;
                int y = topRow + row * 18;
                int slot = col + row * 9 + 9;
                // vanilla Slot is fine for player inventory
                // (AbstractContainerMenu has addSlot(Slot))
                // but simplest:
                this.addSlot(new Slot(inv, slot, x, y));
            }
        }
    }

    private void addPlayerHotbar(Inventory inv, int leftCol, int topRow) {
        for (int col = 0; col < 9; ++col) {
            int x = leftCol + col * 18;
            int y = topRow;
            this.addSlot(new Slot(inv, col, x, y));
        }
    }
}
