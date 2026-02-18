package io.github.daxigua2333.mocai_clues.component.data.renderer;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.system.renderer.PassType;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRendererData {
    public abstract PassType getPassType();

    @Nullable
    public abstract ChunkPos getChunkPos(ClueObject obj);

    public static final Codec<BaseRendererData> CODEC = PassType.CODEC.dispatch(
            "type",
            BaseRendererData::getPassType,
            PassType::codec
    );

}
