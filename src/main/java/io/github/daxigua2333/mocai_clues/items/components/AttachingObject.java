package io.github.daxigua2333.mocai_clues.items.components;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record AttachingObject(UUID id) {
    public static final Codec<AttachingObject> CODEC =
            UUIDUtil.CODEC.xmap(AttachingObject::new, AttachingObject::id);

    public static final StreamCodec<ByteBuf, AttachingObject> STREAM_CODEC =
            UUIDUtil.STREAM_CODEC.map(AttachingObject::new, AttachingObject::id);
}
