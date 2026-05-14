package io.github.daxigua2333.cmagic_clue.data.location.factory;

import io.github.daxigua2333.cmagic_clue.data.location.FromSavedData;
import io.github.daxigua2333.cmagic_clue.data.location.IRuntimeLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FromSavedDataSerializable() implements ISerializableLocation {
    public static final StreamCodec<ByteBuf, FromSavedDataSerializable> STREAM_CODEC = StreamCodec.unit(new FromSavedDataSerializable());

    @Override
    public Type type() {
        return Type.SD;
    }

    @Override
    public IRuntimeLocation create(IPayloadContext context) {
        return new FromSavedData(context.player().level());
    }
}
