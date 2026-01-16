package io.github.daxigua2333.mocai_clues.guis;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.guis.widget.AutoUpdatedScrollableListWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.DetailPanelInClueBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClueBookScreen extends Screen {

    private static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "textures/gui/casebook.png");

    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int PANEL_X_OFFSET = 15;
    private static final int PANEL_Y_OFFSET = 43;
    private static final int PANEL_WIDTH = 243 - PANEL_X_OFFSET;
    private static final int PANEL_HEIGHT = 222 - PANEL_Y_OFFSET;
    private static final int LIST_X_OFFSET = 22;
    private static final int LIST_Y_OFFSET = 56;
    private static final int LIST_WIDTH = 89 - LIST_X_OFFSET;
    private static final int LIST_HEIGHT = 214 - LIST_Y_OFFSET;
    private static final int ENTRY_HEIGHT = 20;
    private static final int SPLIT_X_OFFSET = 97;
    private static final int DETAIL_X_OFFSET = SPLIT_X_OFFSET + 18;
    private static final int DETAIL_WIDTH = 120;


    public ClueBookScreen() {
        super(Component.translatable(String.format("screen.%s.clue_book", MoCaiClues.MODID)));
    }

    @Override
    protected void init() {
        super.init();

        var details = new DetailPanelInClueBook(
                Minecraft.getInstance(),
                DETAIL_WIDTH,
                LIST_HEIGHT - 4,
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET,
                (this.width - TEXTURE_WIDTH) / 2 + DETAIL_X_OFFSET
        );

        var list = new AutoUpdatedScrollableListWidget<>(
                this.minecraft,
                (this.width - TEXTURE_WIDTH) / 2 + LIST_X_OFFSET,
                (this.height - TEXTURE_HEIGHT) / 2 + LIST_Y_OFFSET,
                1,
                LIST_WIDTH, LIST_HEIGHT,
                ENTRY_HEIGHT,
                () -> getHolder() == null ? List.of() : new ArrayList<>(getHolder().values()),
                (obj) -> obj.getId(),
//                Component::literal,
                (clue) -> {
//                    Component.literal(clue.getId().toString());
                    InfoData compo = clue.getComponent(ComponentType.INFO_DATA);
                    if (compo == null) return Component.empty();
                    return Component.literal(compo.getName());
                },
                (clueObject) -> {
                    if (clueObject == null) return;
                    details.updateObject(clueObject);
                }
        );

        addRenderableWidget(list);
        addRenderableWidget(details);

    }

    @Nullable
    private ObjectHolder<ClueObject> getHolder() {
        Player p = Minecraft.getInstance().player;
        if (p == null) return null;
        return p.getData(ModAttachmentRegistry.CLUE_BOOK);
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
        int left = (this.width - TEXTURE_WIDTH) / 2;
        int top = (this.height - TEXTURE_HEIGHT) / 2;

        // book texture
        gfx.blit(BOOK_TEXTURE, left, top, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
