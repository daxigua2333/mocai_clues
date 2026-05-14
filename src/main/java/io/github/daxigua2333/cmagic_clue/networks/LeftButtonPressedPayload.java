package io.github.daxigua2333.cmagic_clue.networks;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LeftButtonPressedPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<LeftButtonPressedPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "left_button_pressed_payload"));

    public static final StreamCodec<ByteBuf, LeftButtonPressedPayload> STREAM_CODEC = StreamCodec.unit(new LeftButtonPressedPayload());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
