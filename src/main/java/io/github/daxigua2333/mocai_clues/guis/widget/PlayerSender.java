package io.github.daxigua2333.mocai_clues.guis.widget;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.integration.CmagicCompat;
import io.github.daxigua2333.mocai_clues.integration.DependencyManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerSender extends AbstractContainerWidget {
    private @Nullable PlayerData selected;

    private static final Font FONT = Minecraft.getInstance().font;
    private static final Component PREFIX = Component.translatable(MoCaiClues.MODID + ".screen.share_with");
    private final ScrollableDropdownWidget<PlayerData> dropdown;
    private final Button button;

    private static final int BUTTON_WIDTH = 30;

    public record PlayerData(UUID playerId, Component name) {
    }

    public PlayerSender(
            int x, int y, int z, int width, int height,
            Consumer<@Nullable PlayerData> onSend) {
        super(x, y, width, height, Component.empty());

        int prefixWidth = FONT.width(PREFIX);
        dropdown = new ScrollableDropdownWidget<>(x + prefixWidth + 2, y, z, width - prefixWidth - BUTTON_WIDTH - 4, 0,
                () -> {
                    var mc = Minecraft.getInstance();
                    ClientPacketListener conn = mc.getConnection();
                    if (mc.player == null || conn == null) return Collections.emptyList();
                    List<PlayerData> result = new ArrayList<>(conn.getOnlinePlayers().size());
                    for (PlayerInfo info : conn.getOnlinePlayers()) {
                        if (info.getProfile().getId().equals(mc.player.getUUID())) {
                            continue;
                        }
                        result.add(new PlayerData(
                                info.getProfile().getId(),
                                info.getTabListDisplayName() == null ? Component.literal(info.getProfile().getName()) : info.getTabListDisplayName()
                        ));
                    }
                    if (DependencyManager.isLoaded(DependencyManager.Mod.C_MAGIC)) {
                        if (CmagicCompat.isPerforming()) {
                            return result.stream().filter(data -> CmagicCompat.getAllMajoDecoName().contains(data.name)).toList();
                        }
                    }
                    return result;
                },
                data -> {
                    if (data == null) return Component.empty();
                    return data.name();
                },
                PlayerData::playerId,
                data -> selected = data);

        button = Button.builder(Component.translatable(MoCaiClues.MODID + ".screen.share"),
                        btn -> onSend.accept(selected))
                .bounds(x + width + -BUTTON_WIDTH, y, BUTTON_WIDTH, height)
                .build();

    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.drawString(FONT, PREFIX, getX(), getY() + height / 2 - FONT.lineHeight / 2, 0x00000000, false);
        dropdown.render(graphics, mouseX, mouseY, partialTick);
        button.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(dropdown, button);
    }
}
