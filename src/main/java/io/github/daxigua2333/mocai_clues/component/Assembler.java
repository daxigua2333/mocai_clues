package io.github.daxigua2333.mocai_clues.component;

import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.network.ManualClueServerHandler;
import io.github.daxigua2333.mocai_clues.component.storage.SavedDataHolder;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class Assembler {

    // ===== manual clue =====
    public static ClueObject createManualClue(String name, List<String> details, ServerLevel level){
        ClueObject object = new ClueObject(ClueType.MANUAL);
        object.addComponent(new DetailData(name, details));
        object.addComponent(new ManualClueServerHandler());
        object.addComponent(new SavedDataHolder(level));
        return object;
    }
    public static ClueObject createManualClue(ServerLevel level) {
        return Assembler.createManualClue("default name", new ArrayList<>(), level);
    }

}
