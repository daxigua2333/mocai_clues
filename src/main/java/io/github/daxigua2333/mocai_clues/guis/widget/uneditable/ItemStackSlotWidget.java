package io.github.daxigua2333.mocai_clues.guis.widget.uneditable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ItemStackSlotWidget extends AbstractWidget {
    private static final ResourceLocation SLOT_SPRITE =
            ResourceLocation.withDefaultNamespace("container/slot");

    private final Supplier<ItemStack> stackSupplier;

    public ItemStackSlotWidget(int x, int y, Supplier<ItemStack> stackSupplier) {
        super(x, y, 18, 18, Component.empty());
        this.stackSupplier = stackSupplier;
//        this.active = false; // visual only
    }

    @Override
    protected void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        gg.blitSprite(SLOT_SPRITE, getX(), getY(), 18, 18);

        ItemStack stack = stackSupplier.get();
        if (!stack.isEmpty()) {
            gg.renderItem(stack, getX() + 1, getY() + 1);
            gg.renderItemDecorations(Minecraft.getInstance().font, stack, getX() + 1, getY() + 1);
            if (this.isHovered() &&
                    mouseX >= getX() && mouseX <= getX() + 18 && mouseY >= getY() && mouseY <= getY() + 18) {
                gg.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        // no narration
    }
}
