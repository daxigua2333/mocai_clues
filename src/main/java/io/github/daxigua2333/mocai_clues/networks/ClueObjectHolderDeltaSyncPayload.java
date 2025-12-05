package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.DeltaType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.UUID;

public record ClueObjectHolderDeltaSyncPayload(DeltaType deltaType, @Nullable UUID id, @Nullable ClueObject object) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClueObjectHolderDeltaSyncPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "clue_object_holder_delta_sync_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClueObjectHolderDeltaSyncPayload> STREAM_CODEC =
            StreamCodec.of(
                    (RegistryFriendlyByteBuf buf, ClueObjectHolderDeltaSyncPayload payload) -> {
                        DeltaType.STREAM_CODEC.encode(buf, payload.deltaType);
                        switch (payload.deltaType) {
                            case PUT -> {
                                UUIDUtil.STREAM_CODEC.encode(buf, payload.id);
                                ClueObject.STREAM_CODEC.encode(buf, payload.object);
                            }
                            case REMOVE -> {
                                UUIDUtil.STREAM_CODEC.encode(buf, payload.id);
                            }
                            case CLEAR -> {

                            }
                        }
                    },
                    buf -> {
                        DeltaType type = DeltaType.STREAM_CODEC.decode(buf);
                        switch (type) {
                            case PUT -> {return new ClueObjectHolderDeltaSyncPayload(
                                    type,
                                    UUIDUtil.STREAM_CODEC.decode(buf),
                                    ClueObject.STREAM_CODEC.decode(buf)
                            );}
                            case REMOVE -> {return new ClueObjectHolderDeltaSyncPayload(
                                    type,
                                    UUIDUtil.STREAM_CODEC.decode(buf),
                                    null
                            );}
                            case CLEAR -> {return new ClueObjectHolderDeltaSyncPayload(
                                    type,
                                    null,
                                    null
                            );}
                            case null, default -> throw new NullPointerException("why... this is impossible :(");
                        }
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
