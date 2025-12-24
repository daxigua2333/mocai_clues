package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// TODO: route to SD or something...
public record WandSwitchToAttachModePayload(ClueObject object) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WandSwitchToAttachModePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "wand_switch_to_attach_mode_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WandSwitchToAttachModePayload> STREAM_CODEC =
            StreamCodec.of(
                    (RegistryFriendlyByteBuf buf, WandSwitchToAttachModePayload payload) -> {
                        ClueObject.STREAM_CODEC.encode(buf, payload.object);
                    },
                    buf -> {
                        return new WandSwitchToAttachModePayload(ClueObject.STREAM_CODEC.decode(buf));
                    }
            );
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
