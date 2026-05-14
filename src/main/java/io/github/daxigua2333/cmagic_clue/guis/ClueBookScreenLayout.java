package io.github.daxigua2333.cmagic_clue.guis;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.guis.widget.FixedTextureButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ClueBookScreenLayout extends Screen {
    private static final ResourceLocation BOOK_SPRITE =
            ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "clue_book_bg");

    private static final ResourceLocation CLOSE_BUTTON =
//            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "textures/gui/clue_book_close_button.png");
            ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "clue_book_close_button");
    //    protected static final int CLOSE_BUTTON_W = 68;
//    protected static final int CLOSE_BUTTON_H = 36;
    protected static final int CLOSE_BUTTON_W = 90;
    protected static final int CLOSE_BUTTON_H = 36;

    protected static final int LIST_W = 90;
    protected static final int LIST_H = 200;
    protected static final int PANEL_W = 200;
    protected static final int PANEL_H = LIST_H + CLOSE_BUTTON_H;
    protected static final int SPLIT_W = 4;

    protected static final int WHOLE_W = LIST_W + SPLIT_W + PANEL_W;
    protected static final int WHOLE_H = PANEL_H;

    protected static final int CLOSE_BUTTON_X_OFFSET = 0;
    protected static final int CLOSE_BUTTON_Y_OFFSET = 0;
    protected static final int LIST_X_OFFSET = 0;
    protected static final int LIST_Y_OFFSET = CLOSE_BUTTON_H;
    protected static final int PANEL_X_OFFSET = LIST_X_OFFSET + LIST_W + SPLIT_W;
    protected static final int PANEL_Y_OFFSET = CLOSE_BUTTON_Y_OFFSET;


    protected ClueBookScreenLayout(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        int left = (width - WHOLE_W) / 2;
        int top = (height - WHOLE_H) / 2;
        FixedTextureButton closeButton = new FixedTextureButton(
                left + CLOSE_BUTTON_X_OFFSET, top + CLOSE_BUTTON_Y_OFFSET, CLOSE_BUTTON_W, CLOSE_BUTTON_H,
                Component.empty(),
                btn -> this.onClose(),
                CLOSE_BUTTON
        );

        addRenderableWidget(closeButton);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);

        // render widgets
        super.render(gfx, mouseX, mouseY, partialTick);

//        // Tooltips (e.g. from item icon)
//        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - WHOLE_W) / 2;
        int top = (this.height - WHOLE_H) / 2;

        // book texture
//        gfx.blit(BOOK_TEXTURE, left, top, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
//        gfx.blitSprite(ResourceLocation.withDefaultNamespace("widget/button"), 0, 0, 500, 20);
//        gfx.blitSprite(BOOK_SPRITE, 0, 0, 400, 200);
        gfx.blitSprite(BOOK_SPRITE, left + LIST_X_OFFSET, top + LIST_Y_OFFSET, LIST_W, LIST_H);
        gfx.blitSprite(BOOK_SPRITE, left + PANEL_X_OFFSET, top + PANEL_Y_OFFSET, PANEL_W, PANEL_H);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
