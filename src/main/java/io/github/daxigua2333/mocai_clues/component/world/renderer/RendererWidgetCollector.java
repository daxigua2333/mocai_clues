package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.gui.editable.MultiChoiceList;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.ScaledTextRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.SplitLineRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.TextListWithIndexRow;
import io.github.daxigua2333.mocai_clues.networks.WandSwitchToAttachModePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RendererWidgetCollector extends ClueComponent{
    private final List<ComponentType> list;
    public List<ComponentType> getList() {
        return list;
    }

    public RendererWidgetCollector(List<ComponentType> list) {
        this.list = list;
    }

    @Override
    public ComponentType type() {
        return ComponentType.RENDERER_WIDGET_COLLECTOR;
    }

    public static final Codec<RendererWidgetCollector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentType.CODEC.listOf().fieldOf("list").forGetter(RendererWidgetCollector::getList)
    ).apply(instance, RendererWidgetCollector::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable() {
        return List.of(
                Button.builder(Component.translatable("attach"), btn -> {
                    // TODO:
                    PacketDistributor.sendToServer(new WandSwitchToAttachModePayload(this.owner));
                    Minecraft.getInstance().setScreen(null);
                }).bounds(0, 0, 0, 20).build(),
                new MultiChoiceList(0, 0, 0, 20, list,
                        (added) -> {
                            var compo = ComponentType.getPass(added);
                            if (compo == null) throw new RuntimeException("unsupported renderer component type: " + added.toString());
                            this.owner.addComponent(compo);
                        },
                        (removed) -> this.owner.removeComponent(removed))
        );
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        List<AbstractWidget> result = new ArrayList<>();

        return result;
//        return List.of(
//                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.literal(name), 1.2f),
//                new SplitLineRow(0, 0, 100, 100, 2, 2),
//                new TextListWithIndexRow(0, 0, 100, 100, 2, 2, details, 2)
//        );
    }
}
