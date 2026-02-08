//package io.github.daxigua2333.mocai_clues.data.location.factory;
//
//import io.github.daxigua2333.mocai_clues.data.location.FromServerSavedData;
//import io.github.daxigua2333.mocai_clues.data.location.IRuntimeLocation;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.network.codec.StreamCodec;
//import net.neoforged.neoforge.network.handling.IPayloadContext;
//
//public class FromServerSavedDataSerializable implements ISerializableLocation {
//    public static final StreamCodec<ByteBuf, FromServerSavedDataSerializable> STREAM_CODEC = StreamCodec.unit(new FromServerSavedDataSerializable());
//
//    @Override
//    public Type type() {
//        return Type.SERVER_SD;
//    }
//
//    @Override
//    public IRuntimeLocation create(IPayloadContext context) {
//        return new FromServerSavedData(context.player().getServer());
//    }
//}
