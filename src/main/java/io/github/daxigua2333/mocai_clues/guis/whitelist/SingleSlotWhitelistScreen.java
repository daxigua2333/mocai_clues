package io.github.daxigua2333.mocai_clues.guis.whitelist;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.guis.ManualClueEditorScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SingleSlotWhitelistScreen extends AbstractContainerScreen<WhitelistMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "textures/gui/single_slot_container.png");
    private static final int BG_W = 176;
    private static final int BG_H = 112;

    public SingleSlotWhitelistScreen(WhitelistMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
//        this.imageHeight = 114 + this.containerRows * 18;
//        this.inventoryLabelY = this.imageHeight - 94;
    }


    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - BG_W) / 2;
        int y = (this.height - BG_H) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
//        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, BG_W, BG_H);
//        guiGraphics.blit(CONTAINER_BACKGROUND, x, y + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void onClose() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
//        player.closeContainer();
        player.connection.send(new ServerboundContainerClosePacket(player.containerMenu.containerId));
        player.containerMenu = player.inventoryMenu;
//        this.minecraft.setScreen((Screen)null);

        mc.popGuiLayer();

        if (Minecraft.getInstance().screen instanceof ManualClueEditorScreen screen) {
            WhitelistMenu oldMenu = this.menu;
            screen.setItemClue(oldMenu.getItemStack());
        }

    }

}
