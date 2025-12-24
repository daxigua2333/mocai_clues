package io.github.daxigua2333.mocai_clues.component;

import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.world.data.WorldBlockPos;
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
                ComponentType.BLOCK_OUTLINE_RENDERER
//                ComponentType.FLASH_POINT_RENDERER
                )));
        object.addComponent(new WorldBlockPos(new BlockPos(0, -60, 0)));
        object.addComponent(new BlockOutlinePass());
        return object;
    }
    public static ClueObject createManualClue() {
        return Assembler.createManualClue("default name", new ArrayList<>());
    }

}
