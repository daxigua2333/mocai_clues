package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BasePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BlockOutlinePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.FlashPointRender;
import org.jetbrains.annotations.Nullable;

public enum ComponentType {
    // TODO
    // data
    DETAIL_DATA,
    // world renderer
    RENDERER_WIDGET_COLLECTOR,
    BLOCK_POS_LIST,

    BLOCK_OUTLINE_RENDERER,
    FLASH_POINT_RENDERER,

    ;

    public static final Codec<ComponentType> CODEC =
            Codec.STRING.xmap(ComponentType::valueOf, Enum::name);

    @Nullable
    public static BasePass getPass(ComponentType type) {
        switch (type) {
            case BLOCK_OUTLINE_RENDERER -> {
                return new BlockOutlinePass();
            }
            case FLASH_POINT_RENDERER -> {
                return new FlashPointRender();
            }
            case null, default -> {
                return null;
            }
        }
    }
}
