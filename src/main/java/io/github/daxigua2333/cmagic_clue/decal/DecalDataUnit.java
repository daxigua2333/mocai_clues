package io.github.daxigua2333.cmagic_clue.decal;


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.cmagic_clue.utils.EnumCodecProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.nio.ByteBuffer;

public record DecalDataUnit(Type type, Object meta) {
    public enum Type {
        CLIENT,
        STATIC,
        DYNAMIC,
        ;
        public static final Codec<Type> CODEC = EnumCodecProvider.createCodec(Type.class);
    }

    // type check in constructor
    public DecalDataUnit {
//        if (type == Type.CLIENT && !(meta instanceof AtlasRLWithUV)
        if (type == Type.CLIENT && !(meta instanceof AtlasRegion)
                || type == Type.STATIC && !(meta instanceof ResourceLocation)
                || type == Type.DYNAMIC && !(meta instanceof byte[])) {
            throw new IllegalArgumentException(
                    "Invalid type in DecalDataUnit. Unit type: " + type + ". Meta type: " + (meta == null ? "null" : meta.getClass().getName())
            );
        }
        if (type == Type.DYNAMIC && ((byte[]) meta).length != DecalLayerHolder.BYTE_PER_PIXEL * DecalLayerHolder.DYNAMIC_LAYER_SIZE)
            throw new RuntimeException("Wrong dynamic layer size.");
    }

    public static final Codec<DecalDataUnit> CODEC = Type.CODEC.dispatch(
            "type",
            DecalDataUnit::type,
            DecalDataUnit::getCodecForType
    );

    public static final StreamCodec<ByteBuf, DecalDataUnit> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    private static MapCodec<DecalDataUnit> getCodecForType(Type type) {
        return switch (type) {
            case STATIC -> ResourceLocation.CODEC
                    .fieldOf("meta")
                    .xmap(
                            loc -> new DecalDataUnit(Type.STATIC, loc),
                            unit -> (ResourceLocation) unit.meta()
                    );
            case DYNAMIC -> Codec.BYTE_BUFFER
                    .fieldOf("meta")
                    .xmap(
                            // Convert ByteBuffer to byte[]
                            buffer -> {
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                return new DecalDataUnit(Type.DYNAMIC, bytes);
                            },
                            // Convert byte[] to ByteBuffer
                            unit -> ByteBuffer.wrap((byte[]) unit.meta())
                    );
            case CLIENT -> Codec.unit(null)
                    .fieldOf("meta")
                    .validate(u -> DataResult.error(() -> "CLIENT decals should never be serialized!"))
                    .xmap(u -> new DecalDataUnit(Type.CLIENT, null), unit -> null);
        };
    }

    public static DecalDataUnit covertS2C(DecalDataUnit sUnit, DecalAtlas atlas) {
        AtlasRegion region;
        switch (sUnit.type) {
            case STATIC -> {
                region = atlas.getStaticRegion((ResourceLocation) sUnit.meta());
            }
            case DYNAMIC -> {
                region = atlas.allocateDynamic();
                atlas.updateDynamic(region, (byte[]) sUnit.meta());
            }
            default -> {
                throw new RuntimeException();
            }
        }

//        return new DecalDataUnit(Type.CLIENT, new AtlasRLWithUV(atlas.getLocation(), region));
        return new DecalDataUnit(Type.CLIENT, region);
    }
}
