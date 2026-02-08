//package io.github.daxigua2333.mocai_clues.data.location.factory;
//
//import io.github.daxigua2333.mocai_clues.data.location.FromClientSavedData;
//import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.network.codec.StreamCodec;
//import net.neoforged.neoforge.network.handling.IPayloadContext;
//
//public record FromClientSavedDataSerializable() implements ISerializableLocation {
//    public static final StreamCodec<ByteBuf, FromClientSavedDataSerializable> STREAM_CODEC = StreamCodec.unit(new FromClientSavedDataSerializable());
//
//    @Override
//    public Type type() {
//        return Type.CLIENT_SD;
//    }
//
//    @Override
//    public IRuntimeLocation create(IPayloadContext context) {
//        return new FromClientSavedData();
//    }
//}
