package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventHolder;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererWidgetCollector;

import java.util.Map;

/** Registry for components */
public enum ComponentType {
    // TODO
    // data
    DETAIL_DATA,
    // world renderer
    RENDERER_WIDGET_COLLECTOR,
    BLOCK_POS_SET,
    RENDERER_HOLDER,

    // interact
    INTERACT_EVENT_HOLDER,

    ;

    public static final Codec<ComponentType> CODEC =
            Codec.STRING.xmap(ComponentType::valueOf, Enum::name);
    public static final Codec<Map<ComponentType, ClueComponent>> MAP_CODEC =
            Codec.dispatchedMap(
                    ComponentType.CODEC,
                    key -> switch (key) {  // TODO
                        case DETAIL_DATA -> DetailData.CODEC;
                        case BLOCK_POS_SET -> BlockPosSet.CODEC;
                        case RENDERER_WIDGET_COLLECTOR -> RendererWidgetCollector.CODEC;
                        case INTERACT_EVENT_HOLDER -> InteractEventHolder.CODEC;
                        case RENDERER_HOLDER -> RendererHolder.CODEC;
                    }
            );


}
