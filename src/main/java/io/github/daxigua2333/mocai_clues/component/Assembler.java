package io.github.daxigua2333.mocai_clues.component;

import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosSet;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventHolder;
import io.github.daxigua2333.mocai_clues.component.world.interact.handler.DefaultHandler;
import io.github.daxigua2333.mocai_clues.component.world.interact.handler.PlaySound;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.FinderHit;
import io.github.daxigua2333.mocai_clues.component.world.renderer.BlockOutlinePass;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererWidgetCollector;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class Assembler {

    // ===== manual clue ===== TODO
    public static ClueObject createManualClue(String name, List<String> details){
        ClueObject object = new ClueObject(ClueType.MANUAL);
        object.addComponent(new DetailData(name, details));
        object.addComponent(new RendererWidgetCollector(List.of(
                ComponentType.BLOCK_OUTLINE_PASS
//                ComponentType.FLASH_POINT_RENDERER
                )));
//        BlockPosSet test = new BlockPosSet();
//        test.add(new BlockPos(0, -60, 0));
//        object.addComponent(test);
        object.addComponent(new BlockOutlinePass());
        object.addComponent(new InteractEventHolder(List.of(
//                new InteractEvent(new FinderHit(), new PlaySound())  // finder
        )));
        return object;
    }
    public static ClueObject createManualClue() {
        return Assembler.createManualClue("default name", new ArrayList<>());
    }

}
