package io.github.daxigua2333.mocai_clues.component.world.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RendererWidgetCollector extends ClueComponent {
    private final List<PassType> list;

    public List<PassType> getList() {
        return list;
    }

    public RendererWidgetCollector(List<PassType> list) {
        this.list = list;
    }

    @Override
    public ComponentType type() {
        return ComponentType.RENDERER_WIDGET_COLLECTOR;
    }

    public static final Codec<RendererWidgetCollector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PassType.CODEC.listOf().fieldOf("list").forGetter(RendererWidgetCollector::getList)
    ).apply(instance, RendererWidgetCollector::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        return List.of(
//                Button.builder(Component.translatable("attach"), btn -> {
//                    PacketDistributor.sendToServer(new WandSwitchToAttachModePayload(this.owner));
//                    Minecraft.getInstance().setScreen(null);
//                }).bounds(0, 0, 0, 20).build()
//                new MultiChoiceList(0, 0, 0, 20, list,
//                        (added) -> {
//                            var compo = PassType.getPass(added);
//                            if (compo == null) throw new RuntimeException("unsupported renderer component type: " + added.toString());
//                            this.owner.addComponent(compo);
//                        },
//                        (removed) -> this.owner.removeComponent(removed))
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
