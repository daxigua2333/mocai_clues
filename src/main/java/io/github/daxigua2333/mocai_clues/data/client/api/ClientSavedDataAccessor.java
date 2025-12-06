package io.github.daxigua2333.mocai_clues.data.client.api;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.client.ClientClueObjectMainMapSavedData;
import io.github.daxigua2333.mocai_clues.data.client.ClientObjectHolderInSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientSavedDataAccessor {
    public static List<ClueObject> queryClueObjectByClueType(ClueType type) {
        // TODO: cache index map to optimize
        List<ClueObject> result = new ArrayList<>();
//        Map<UUID, ClueObject> map = ClientClueObjectMainMapSavedData.getInstance().getMap();
        ObjectHolder<ClueObject> map = ClientObjectHolderInSavedData.getInstance().getHolder();
        for (ClueObject obj : map.values()) {
            if (obj.type() == type) {
                result.add(obj);
            }
        }
        return result;
    }
}
