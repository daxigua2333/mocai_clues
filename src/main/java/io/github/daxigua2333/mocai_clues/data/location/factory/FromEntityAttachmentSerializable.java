package io.github.daxigua2333.mocai_clues.data.location.factory;

import io.github.daxigua2333.mocai_clues.data.location.FromEntityAttachment;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record FromEntityAttachmentSerializable(UUID entityId) implements ISerializableLocation {
    public static final StreamCodec<ByteBuf, FromEntityAttachmentSerializable> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, FromEntityAttachmentSerializable::entityId,
            FromEntityAttachmentSerializable::new
    );

    @Override
    public Type type() {
        return Type.ENTITY;
    }

    @Override
    public IRuntimeLocation create(IPayloadContext context) {
        if (context.player().level() instanceof ServerLevel level) {
            return new FromEntityAttachment(level, entityId);
        } else {
            throw new RuntimeException("Invalid Dist.");
        }
    }

}
