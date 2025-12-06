package io.github.daxigua2333.mocai_clues.data.client;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.networks.ClientActivelySyncSavedDataPayload;
import io.github.daxigua2333.mocai_clues.networks.ClueObjectHolderDeltaSyncPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientClueObjectMainMapSavedData {
    private static Map<UUID, ClueObject> map = new LinkedHashMap<>();
    private static final ClientClueObjectMainMapSavedData INSTANCE = new ClientClueObjectMainMapSavedData();
    private ClientClueObjectMainMapSavedData() {
        PacketDistributor.sendToServer(new ClientActivelySyncSavedDataPayload());
    }

    public static ClientClueObjectMainMapSavedData getInstance() {
        return INSTANCE;
    }

    public void setMap(Map<UUID, ClueObject> map) {
        ClientClueObjectMainMapSavedData.map = new LinkedHashMap<>(map);
    }
    public Map<UUID, ClueObject> getMap() {
        return map;
    }

    // ====== delta sync handler ======
    // the list is used to update Screen
    private final List<ClueObjectHolderDeltaSyncPayload> deltas = new ArrayList<>();
    public void addToDeltas(ClueObjectHolderDeltaSyncPayload delta) {
        deltas.add(delta);
    }
    // used to update data holder
    public void applyDelta(ClueObjectHolderDeltaSyncPayload delta) {
        switch (delta.deltaType()) {
            case PUT -> map.put(delta.id(), delta.object());
            case REMOVE -> map.remove(delta.id());
            case CLEAR -> map.clear();
            case null, default -> throw new NullPointerException("no....>_<...please no....");
        }
    }
}
