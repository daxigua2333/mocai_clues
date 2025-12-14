package io.github.daxigua2333.mocai_clues.data.client.api;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.data.client.ClientDatabase;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.NitriteMyObjectStore;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * entry to access all the client data
 * in all data holders, including SavedData, chunk/entity/ attachment, item...
 * TODO: use polymorphism to include all data holders
 */
public class ClientAccessor {


    public static List<ClueObject> queryClueObjectByClueType(ClueType type) {
        List<ClueObject> result = new ArrayList<>();
//        result.addAll(ClientSavedDataAccessor.queryClueObjectByClueType(type));
//        result.addAll();
        NitriteMyObjectStore store = MyObjectSync.client().store();
//        MoCaiClues.LOGGER.debug("{}", store.find());

        return store.find();
    }

}
