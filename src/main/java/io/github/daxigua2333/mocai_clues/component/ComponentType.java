package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BasePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BlockOutlinePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.FlashPointRender;
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
    BLOCK_POS_LIST,

    BLOCK_OUTLINE_PASS,
    FLASH_POINT_PASS,

    ;

    public static final Codec<ComponentType> CODEC =
            Codec.STRING.xmap(ComponentType::valueOf, Enum::name);

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
