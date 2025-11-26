package io.github.daxigua2333.mocai_clues.footprints.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;


public record Footprint (
        double x,
        double y,
        double z,
        float rotation,
        float longSide,
        float shortSide,
        float alpha
) {

    public static final Codec<Footprint> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter(Footprint::x),
                Codec.DOUBLE.fieldOf("y").forGetter(Footprint::y),
                Codec.DOUBLE.fieldOf("z").forGetter(Footprint::z),
                Codec.FLOAT.fieldOf("rotation").forGetter(Footprint::rotation),
                Codec.FLOAT.fieldOf("longSide").forGetter(Footprint::longSide),
                Codec.FLOAT.fieldOf("shortSide").forGetter(Footprint::shortSide),
                Codec.FLOAT.fieldOf("alpha").forGetter(Footprint::alpha)
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
                buf.readFloat()
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
    }
    public static final StreamCodec<ByteBuf, Footprint> STREAM_CODEC =
            StreamCodec.ofMember(Footprint::encode, Footprint::new);

}
