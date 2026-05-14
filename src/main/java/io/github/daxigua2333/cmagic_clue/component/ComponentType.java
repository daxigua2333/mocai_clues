package io.github.daxigua2333.cmagic_clue.component;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.cmagic_clue.component.data.BlockPosWithFace;
import io.github.daxigua2333.cmagic_clue.component.data.DetailData;
import io.github.daxigua2333.cmagic_clue.component.data.InfoData;
import io.github.daxigua2333.cmagic_clue.component.data.ItemClue;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.InteractEntry;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.InteractPassiveBehavior;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.InteractResult;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.InteractState;
import io.github.daxigua2333.cmagic_clue.component.data.discovery.cluebook.DetailWithCompleteness;
import io.github.daxigua2333.cmagic_clue.component.data.renderer.RendererHolder;

import java.util.Map;

/**
 * Registry for components
 */
public enum ComponentType {
    // data
    INFO_DATA,
    DETAIL_DATA,
    ITEM_CLUE,
    // world renderer
    BLOCK_POS_WITH_FACE,
    RENDERER_HOLDER,

    // discovery
    DETAIL_WITH_COMPLETENESS,
    INTERACT_STATE,
    INTERACT_ENTRY,
    INTERACT_RESULT,
    INTERACT_PASSIVE_BEHAVIOR,

    ;

    public static final Codec<ComponentType> CODEC =
            Codec.STRING.xmap(ComponentType::valueOf, Enum::name);
    public static final Codec<Map<ComponentType, ClueComponent>> MAP_CODEC =
            Codec.dispatchedMap(
                    ComponentType.CODEC,
                    key -> switch (key) {
                        case INFO_DATA -> InfoData.CODEC;
                        case DETAIL_DATA -> DetailData.CODEC;
                        case RENDERER_HOLDER -> RendererHolder.CODEC;
                        case DETAIL_WITH_COMPLETENESS -> DetailWithCompleteness.CODEC;
                        case ITEM_CLUE -> ItemClue.CODEC;
                        case BLOCK_POS_WITH_FACE -> BlockPosWithFace.CODEC;
                        case INTERACT_ENTRY -> InteractEntry.CODEC;
                        case INTERACT_RESULT -> InteractResult.CODEC;
                        case INTERACT_PASSIVE_BEHAVIOR -> InteractPassiveBehavior.CODEC;
                        case INTERACT_STATE -> InteractState.CODEC;
                    }
            );


}
