package io.github.daxigua2333.mocai_clues.data.server.api;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.CodecNitriteAdapter;
import io.github.daxigua2333.mocai_clues.data.server.ServerDatabase;

import java.util.List;

public final class ServerDataAccessor {
    public static List<ClueObject> queryAll() {
        var db = ServerDatabase.get();
        var coll = db.getCollection("clue_objects");
        var adapter = new CodecNitriteAdapter<ClueObject>(ClueObject.CODEC);
        return coll.find().toList()
                .stream().map(adapter::fromDocument).toList();
    }
}
