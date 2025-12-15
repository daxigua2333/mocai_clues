package io.github.daxigua2333.mocai_clues.data.client.api;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.data.client.ClientDatabase;
import io.github.daxigua2333.mocai_clues.data.sync.MyObjectSync;
import io.github.daxigua2333.mocai_clues.data.sync.NitriteMyObjectStore;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.NitriteCollection;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.FluentFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * entry to access all the client data
 */
public class ClientAccessor {


    public static List<ClueObject> queryClueObjectByClueType(ClueType type) {
        NitriteMyObjectStore store = MyObjectSync.client().store();
        String field = NitriteMyObjectStore.clueField("type");
        return store.retrieve(FluentFilter.where(field).eq(type.toString()));
    }

}
