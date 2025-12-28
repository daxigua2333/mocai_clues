package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BasePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BlockOutlinePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.FlashPointRender;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererWidgetCollector;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/** Registry for components */
public enum ComponentType {
    // TODO
    // data
    DETAIL_DATA,
    // world renderer
    RENDERER_WIDGET_COLLECTOR,
    BLOCK_POS_SET,

    BLOCK_OUTLINE_PASS,
    FLASH_POINT_PASS,

    // interact

    ;

    public static final Codec<ComponentType> CODEC =
            Codec.STRING.xmap(ComponentType::valueOf, Enum::name);
    public static final Codec<Map<ComponentType, ClueComponent>> MAP_CODEC =
            Codec.dispatchedMap(
                    ComponentType.CODEC,
                    key -> switch (key) {  // TODO
                        case DETAIL_DATA -> DetailData.CODEC;
                        case BLOCK_OUTLINE_PASS -> BlockOutlinePass.CODEC;
                        case BLOCK_POS_SET -> BlockPosSet.CODEC;
                        case RENDERER_WIDGET_COLLECTOR -> RendererWidgetCollector.CODEC;
                        case FLASH_POINT_PASS -> FlashPointRender.CODEC;
                    }
            );


    /** registry for render pass */
    private static final EnumMap<ComponentType, BasePass> PASS_REGISTRY = new EnumMap<>(Map.of(
            BLOCK_OUTLINE_PASS, new BlockOutlinePass(),
            FLASH_POINT_PASS, new FlashPointRender()
    ));
    @Nullable
    public static BasePass getPass(ComponentType type) {
        return PASS_REGISTRY.get(type);
    }
}
