package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.Assembler;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.data.common.DataManager;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ScreenCreateDefaultCluePayload(ClueType clueType,
                                             ISerializableLocation location) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ScreenCreateDefaultCluePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "manual_clue_create_payload"));

    public static final StreamCodec<ByteBuf, ScreenCreateDefaultCluePayload> STREAM_CODEC = StreamCodec.composite(
            ClueType.STREAM_CODEC, ScreenCreateDefaultCluePayload::clueType,
            ISerializableLocation.Type.DISPATCH_CODEC, ScreenCreateDefaultCluePayload::location,
            ScreenCreateDefaultCluePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ScreenCreateDefaultCluePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            DataManager.Server.upsert(
                    payload.location.create(context),
                    Assembler.createDefaultByType(payload.clueType)
            );
        });
    }
}
