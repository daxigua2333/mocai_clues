package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


public record ClueObjectMainMapSyncPayload(Map<UUID, ClueObject> map) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClueObjectMainMapSyncPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "manual_clue_main_map_sync_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClueObjectMainMapSyncPayload> STREAM_CODEC =
            StreamCodec.of(
                    (RegistryFriendlyByteBuf buf, ClueObjectMainMapSyncPayload payload) -> {
                        Map<UUID, ClueObject> map = payload.map();
                        buf.writeInt(map.size());
                        for (ClueObject value : map.values()) {
                            ClueObject.STREAM_CODEC.encode(buf, value);
                        }
                    },
                    buf -> {
                        int size = buf.readInt();
                        Map<UUID, ClueObject> map = new LinkedHashMap<>(size);
                        for (int i=0; i<size; i++) {
                            ClueObject value = ClueObject.STREAM_CODEC.decode(buf);
                            map.put(value.getId(), value);
                        }
                        return new ClueObjectMainMapSyncPayload(map);
                    }
            );


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
