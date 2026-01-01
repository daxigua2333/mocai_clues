package io.github.daxigua2333.mocai_clues.component;

import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventHolder;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.component.world.renderer.pass.BlockOutlinePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererWidgetCollector;

import java.util.ArrayList;
import java.util.List;

public class Assembler {

    // ===== manual clue ===== TODO
    public static ClueObject createManualClue(String name, List<String> details){
        ClueObject object = new ClueObject(ClueType.MANUAL);
        object.addComponent(new DetailData(name, details));
        object.addComponent(new RendererWidgetCollector(List.of(
                PassType.BLOCK_OUTLINE
        )));
//        BlockPosSet test = new BlockPosSet();
//        test.add(new BlockPos(0, -60, 0));
//        object.addComponent(test);
        object.addComponent(new RendererHolder(List.of(
                PassType.BLOCK_OUTLINE
        )));
        object.addComponent(new InteractEventHolder(List.of(
//                new InteractEvent(new FinderHit(), new PlaySound())  // finder
        )));
        return object;
    }
    public static ClueObject createManualClue() {
        return Assembler.createManualClue("default name", new ArrayList<>());
    }

}
