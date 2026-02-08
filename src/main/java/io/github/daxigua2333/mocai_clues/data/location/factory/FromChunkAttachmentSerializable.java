package io.github.daxigua2333.mocai_clues.data.location.factory;

import io.github.daxigua2333.mocai_clues.data.location.FromChunkAttachment;
import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FromChunkAttachmentSerializable(ChunkPos chunkPos) implements ISerializableLocation {
    public static final StreamCodec<ByteBuf, FromChunkAttachmentSerializable> STREAM_CODEC = StreamCodec.of(
            (buf, inst) -> {
                buf.writeLong(inst.chunkPos.toLong());
            },
            buf -> new FromChunkAttachmentSerializable(new ChunkPos(buf.readLong()))
    );

    @Override
    public Type type() {
        return Type.CHUNK;
    }

    @Override
    public IRuntimeLocation create(IPayloadContext context) {
        return new FromChunkAttachment(context.player().level(), chunkPos);
    }
}
