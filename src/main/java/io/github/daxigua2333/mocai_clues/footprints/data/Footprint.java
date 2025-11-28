package io.github.daxigua2333.mocai_clues.footprints.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;


public record Footprint (
        double x,
        double y,
        double z,
        float rotation,
        float longSide,
        float shortSide,
        float alpha,
        long createdTime,
        int lifetime,
        UUID ownerUUID
) {

    public static final Codec<Footprint> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(Footprint::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Footprint::y),
                Codec.DOUBLE.fieldOf("z").forGetter(Footprint::z),
                Codec.FLOAT.fieldOf("rotation").forGetter(Footprint::rotation),
                Codec.FLOAT.fieldOf("longSide").forGetter(Footprint::longSide),
                Codec.FLOAT.fieldOf("shortSide").forGetter(Footprint::shortSide),
                Codec.FLOAT.fieldOf("alpha").forGetter(Footprint::alpha),
                Codec.LONG.fieldOf("createdTime").forGetter(Footprint::createdTime),
                Codec.INT.fieldOf("lifetime").forGetter(Footprint::lifetime),
                UUIDUtil.CODEC.fieldOf("ownerUUID").forGetter(Footprint::ownerUUID)
        ).apply(instance, Footprint::new)
    );


    // stream codec
    public Footprint(ByteBuf buf) {
        this(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readLong(),
                buf.readInt(),
                UUIDUtil.STREAM_CODEC.decode(buf)
        );
    }
    public void encode(ByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.rotation);
        buf.writeFloat(this.longSide);
        buf.writeFloat(this.shortSide);
        buf.writeFloat(this.alpha);
        buf.writeLong(this.createdTime);
        buf.writeInt(this.lifetime);
        UUIDUtil.STREAM_CODEC.encode(buf, this.ownerUUID);
    }
    public static final StreamCodec<ByteBuf, Footprint> STREAM_CODEC =
            StreamCodec.ofMember(Footprint::encode, Footprint::new);

}
