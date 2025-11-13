package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FinderHitResultPayload(boolean success) implements CustomPacketPayload{
    public static final CustomPacketPayload.Type<FinderHitResultPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "finder_hit_payload"));

//    public static final StreamCodec<ByteBuf, LeftButtonPressedPayload> STREAM_CODEC = StreamCodec.unit(new LeftButtonPressedPayload());
    public static final StreamCodec<ByteBuf, FinderHitResultPayload> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, FinderHitResultPayload::success,
        FinderHitResultPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
