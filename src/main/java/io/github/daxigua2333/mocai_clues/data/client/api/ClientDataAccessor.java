package io.github.daxigua2333.mocai_clues.data.client.api;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;

import java.util.ArrayList;
import java.util.List;

/**
 * entry to access all the client data
 * in all data holders, including SavedData, chunk/entity/ attachment, item...
 * TODO: use polymorphism to include all data holders
 */
public class ClientDataAccessor {


    public List<ClueObject> queryClueObjectByClueType(ClueType type) {
        List<ClueObject> result = new ArrayList<>();
        result.addAll(ClientSavedDataAccessor.queryClueObjectByClueType(type));
//        result.addAll();
        return result;
    }

}
