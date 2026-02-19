package io.github.daxigua2333.mocai_clues.data.location.factory;

import io.github.daxigua2333.mocai_clues.data.location.FromClueBook;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record FromClueBookSerializable(UUID entityId) implements ISerializableLocation {
    public static final StreamCodec<ByteBuf, FromClueBookSerializable> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, FromClueBookSerializable::entityId,
            FromClueBookSerializable::new
    );

    @Override
    public Type type() {
        return Type.CLUE_BOOK;
    }

    @Override
    public IRuntimeLocation create(IPayloadContext context) {
        if (context.player().level() instanceof ServerLevel level) {
            return new FromClueBook(level, entityId);
        } else {
            throw new RuntimeException("Invalid Dist.");
        }
    }

}
