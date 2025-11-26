package io.github.daxigua2333.mocai_clues.footprints;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

//public class Footprint {
//    public final double x, y, z;      // world position (center)
//    public final float yaw;           // horizontal rotation in degrees or radians
//    public final float size;          // size or half-size
//    public final long expirationTime; // optional: for fading/removal
//    public final ResourceLocation texture; // or index into a shared atlas
//
//    public Footprint(double x, double y, double z, float yaw, float size, ResourceLocation texture, long expirationTime) {
//        this.x = x;
//        this.y = y;
//        this.z = z;
//        this.yaw = yaw;
//        this.size = size;
//        this.texture = texture;
//        this.expirationTime = expirationTime;
//    }
//    public double x, y, z;
//
//    public float rotation;  // horizontal rotation in degrees (0‑360)
//    public float longSide;   // along local “forward” axis
//    public float shortSide;  // perpendicular side
//    public float alpha;      // 0f..1f
//
//    public Footprint(double x, double y, double z,
//                     float rotation,
//                     float longSide, float shortSide,
//                     float alpha) {
//        this.x = x;
//        this.y = y;
//        this.z = z;
//        this.rotation = rotation;
//        this.longSide = longSide;
//        this.shortSide = shortSide;
//        this.alpha = alpha;
//    }
//}

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
//    public static final StreamCodec<ByteBuf, Footprint> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.DOUBLE, Footprint::x,
//            ByteBufCodecs.DOUBLE, Footprint::y,
//            ByteBufCodecs.DOUBLE, Footprint::z,
//            ByteBufCodecs.FLOAT, Footprint::rotation,
//            ByteBufCodecs.FLOAT, Footprint::longSide,
//            ByteBufCodecs.FLOAT, Footprint::shortSide,
//            ByteBufCodecs.FLOAT, Footprint::alpha,
//            Footprint::new
//    );
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
