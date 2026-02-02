package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.component.world.data.AttachedEntitySet;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.finder.*;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererWidgetCollector;

import java.util.Map;

/** Registry for components */
public enum ComponentType {
    // TODO
    // data
    INFO_DATA,
    SWITCH_BETWEEN_ITEM_OR_NONE,
    DETAIL_DATA,
    // world renderer
    RENDERER_WIDGET_COLLECTOR,
    BLOCK_POS_SET,
    ATTACHED_ENTITY_SET,
    RENDERER_HOLDER,

    // finder
    FINDER_STATE,
    FLASH_DOT_SET,
    FOUND_SOURCE,
    DETAIL_WITH_COMPLETENESS,
    CLICK_WITH_FINDER,
    WALK_ON,
    SEND_CLUE,
    ITEM_CLUE,

    ;

    public static final Codec<ComponentType> CODEC =
            Codec.STRING.xmap(ComponentType::valueOf, Enum::name);
    public static final Codec<Map<ComponentType, ClueComponent>> MAP_CODEC =
            Codec.dispatchedMap(
                    ComponentType.CODEC,
                    key -> switch (key) {  // TODO
                        case INFO_DATA -> InfoData.CODEC;
                        case DETAIL_DATA -> DetailData.CODEC;
                        case BLOCK_POS_SET -> BlockPosSet.CODEC;
                        case RENDERER_WIDGET_COLLECTOR -> RendererWidgetCollector.CODEC;
                        case RENDERER_HOLDER -> RendererHolder.CODEC;
                        case FINDER_STATE -> FinderState.CODEC;
                        case SEND_CLUE -> SendClue.CODEC;
                        case FLASH_DOT_SET -> FlashDotSet.CODEC;
                        case FOUND_SOURCE -> FoundSource.CODEC;
                        case DETAIL_WITH_COMPLETENESS -> DetailWithCompleteness.CODEC;
                        case ATTACHED_ENTITY_SET -> AttachedEntitySet.CODEC;
                        case CLICK_WITH_FINDER -> ClickWithFinder.CODEC;
                        case WALK_ON -> WalkOn.CODEC;
                        case ITEM_CLUE -> ItemClue.CODEC;
                        case SWITCH_BETWEEN_ITEM_OR_NONE -> SwitchBetweenItemOrNone.CODEC;
                    }
            );


}
