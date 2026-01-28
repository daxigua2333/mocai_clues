package io.github.daxigua2333.mocai_clues.guis;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class InventoryItemPickerScreen extends Screen {
    // Same texture ChestScreen uses
    private static final ResourceLocation GENERIC_54 =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    // We fake a "1-row container" just to reuse the vanilla background proportions.
    private static final int CONTAINER_ROWS = 1;
    private static final int IMAGE_WIDTH = 176;
    private static final int IMAGE_HEIGHT = 114 + CONTAINER_ROWS * 18; // vanilla ChestScreen logic

    // Slot layout numbers mirror ChestMenu
    private static final int SLOT_SIZE = 16;
    private static final int SLOT_SPACING = 18;
    private static final int INV_X = 8;

    // Put our single "ghost slot" in the center of the 9-slot row
    private static final int GHOST_SLOT_X = 8 + 4 * SLOT_SPACING; // centered
    private static final int GHOST_SLOT_Y = 18;

    private final Screen parent;
    private final Consumer<ItemStack> onResult;

    private int leftPos;
    private int topPos;

    private ItemStack selected;
    private int selectedInventorySlot = -1; // just for highlighting
    private boolean applied;

    public InventoryItemPickerScreen(Screen parent, ItemStack initial, Consumer<ItemStack> onResult) {
        super(Component.literal("Pick an Item"));
        this.parent = parent;
        this.onResult = onResult;
        this.selected = initial.copy();
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;

//        // Optional "Done" button (ESC/E also works)
//        this.addRenderableWidget(
//                Button.builder(CommonComponents.GUI_DONE, b -> closeAndReturn())
//                        .pos(leftPos + IMAGE_WIDTH - 62, topPos + 6)
//                        .size(56, 20)
//                        .build()
//        );

        // Optional: try to find & highlight the first matching stack in the player inventory
        Inventory inv = this.minecraft.player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (ItemStack.isSameItemSameComponents(inv.getItem(i), this.selected)) {
                this.selectedInventorySlot = i;
                break;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Close on inventory key ('E' default), same style as AbstractContainerScreen
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            closeAndReturn();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers); // ESC handled by Screen -> onClose()
    }

    @Override
    public void onClose() {
        closeAndReturn();
    }

    private void closeAndReturn() {
        if (this.minecraft == null) return;

        if (!applied) {
            applied = true;
            onResult.accept(selected.copy());
        }
//        this.minecraft.setScreen(parent);
        this.minecraft.popGuiLayer();
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg, mouseX, mouseY, partialTick);

        renderBg(gg);
        renderItems(gg);
        renderHighlights(gg);

        gg.drawString(this.font, this.title, this.leftPos + 8, this.topPos + 6, 0x404040, false);

//        super.render(gg, mouseX, mouseY, partialTick);

        ItemStack hovered = getStackUnderMouse(mouseX, mouseY);
        if (!hovered.isEmpty()) {
//            this.renderTooltip(gg, hovered, mouseX, mouseY);
        }
    }

    private void renderBg(GuiGraphics gg) {
        // Same as ChestScreen::renderBg
        gg.blit(GENERIC_54, leftPos, topPos, 0, 0, IMAGE_WIDTH, CONTAINER_ROWS * 18 + 17);
        gg.blit(GENERIC_54, leftPos, topPos + CONTAINER_ROWS * 18 + 17, 0, 126, IMAGE_WIDTH, 96);
    }

    private void renderItems(GuiGraphics gg) {
        Inventory inv = this.minecraft.player.getInventory();

        // Our single "ghost slot": just draw selected stack, doesn't belong to any container
        renderSlotItem(gg, selected, leftPos + GHOST_SLOT_X, topPos + GHOST_SLOT_Y);

        // Player inventory coordinates copied from ChestMenu logic
        int invYOffset = (CONTAINER_ROWS - 4) * 18;

        // 3 rows (slot indexes 9..35)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = col + row * 9 + 9;
                int x = leftPos + INV_X + col * SLOT_SPACING;
                int y = topPos + 103 + row * SLOT_SPACING + invYOffset;
                renderSlotItem(gg, inv.getItem(slot), x, y);
            }
        }

        // hotbar (slot indexes 0..8)
        for (int col = 0; col < 9; col++) {
            int x = leftPos + INV_X + col * SLOT_SPACING;
            int y = topPos + 161 + invYOffset;
            renderSlotItem(gg, inv.getItem(col), x, y);
        }
    }

    private void renderSlotItem(GuiGraphics gg, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;
        gg.renderItem(stack, x, y);
        gg.renderItemDecorations(this.font, stack, x, y);
    }

    private void renderHighlights(GuiGraphics gg) {
        // Highlight ghost slot if selected not empty
        if (!selected.isEmpty()) {
            drawSlotHighlight(gg, leftPos + GHOST_SLOT_X, topPos + GHOST_SLOT_Y);
        }

        // Highlight selected inventory slot (optional)
        if (selectedInventorySlot >= 0) {
            int[] xy = slotToXY(selectedInventorySlot);
            if (xy != null) drawSlotHighlight(gg, xy[0], xy[1]);
        }
    }

    private void drawSlotHighlight(GuiGraphics gg, int itemX, int itemY) {
        // Similar look to vanilla highlight: 18x18 translucent box around 16x16 item
        gg.fill(itemX - 1, itemY - 1, itemX + 16 + 1, itemY + 16 + 1, 0x80FFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);

        // Click ghost slot -> clear selection
        if (isHovering(GHOST_SLOT_X, GHOST_SLOT_Y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
            selected = ItemStack.EMPTY;
            selectedInventorySlot = -1;
            return true;
        }

        // Click in player inventory -> select without moving anything
        int slot = getInventorySlotAt(mouseX, mouseY);
        if (slot >= 0) {
            ItemStack stack = this.minecraft.player.getInventory().getItem(slot);
            if (stack.isEmpty()) {
                selected = ItemStack.EMPTY;
                selectedInventorySlot = -1;
            } else {
                selected = stack.copy();          // or stack.copyWithCount(1) if you want "template" behavior
                selectedInventorySlot = slot;
            }
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private ItemStack getStackUnderMouse(double mouseX, double mouseY) {
        if (isHovering(GHOST_SLOT_X, GHOST_SLOT_Y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) {
            return selected;
        }
        int slot = getInventorySlotAt(mouseX, mouseY);
        if (slot >= 0) return this.minecraft.player.getInventory().getItem(slot);
        return ItemStack.EMPTY;
    }

    private int getInventorySlotAt(double mouseX, double mouseY) {
        int invYOffset = (CONTAINER_ROWS - 4) * 18;

        // 3x9 inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = col + row * 9 + 9;
                int x = INV_X + col * SLOT_SPACING;
                int y = 103 + row * SLOT_SPACING + invYOffset;
                if (isHovering(x, y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) return slot;
            }
        }

        // hotbar
        for (int col = 0; col < 9; col++) {
            int slot = col;
            int x = INV_X + col * SLOT_SPACING;
            int y = 161 + invYOffset;
            if (isHovering(x, y, SLOT_SIZE, SLOT_SIZE, mouseX, mouseY)) return slot;
        }

        return -1;
    }

    private int[] slotToXY(int slot) {
        int invYOffset = (CONTAINER_ROWS - 4) * 18;

        if (slot >= 0 && slot < 9) { // hotbar
            int col = slot;
            return new int[]{leftPos + INV_X + col * SLOT_SPACING, topPos + 161 + invYOffset};
        }

        if (slot >= 9 && slot < 36) { // inventory
            int idx = slot - 9;
            int row = idx / 9;
            int col = idx % 9;
            return new int[]{leftPos + INV_X + col * SLOT_SPACING, topPos + 103 + row * SLOT_SPACING + invYOffset};
        }

        return null;
    }

    // Same idea as AbstractContainerScreen#isHovering: extra 1px padding
    private boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        double relX = mouseX - this.leftPos;
        double relY = mouseY - this.topPos;
        return relX >= x - 1 && relX < x + width + 1
                && relY >= y - 1 && relY < y + height + 1;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
