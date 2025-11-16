package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.items.statics.FinderHitResultTicker;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FinderLeaveHandPayload(boolean isMain) implements CustomPacketPayload {
    // isMain: true is mainHand else offHand
    public static final CustomPacketPayload.Type<FinderLeaveHandPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "finder_leave_hand_payload"));

    public static final StreamCodec<ByteBuf, FinderLeaveHandPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FinderLeaveHandPayload::isMain,
            FinderLeaveHandPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
