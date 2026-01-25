package io.github.daxigua2333.mocai_clues.component;

import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.component.world.finder.*;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererWidgetCollector;
import io.github.daxigua2333.mocai_clues.component.world.renderer.data.BlockOutlineData;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Assembler {

    // ===== manual clue ===== TODO
    private static ClueObject createManualClue(String name, List<String> details) {
        ClueObject object = new ClueObject(ClueType.MANUAL);
        object.addComponent(new InfoData(name));
        object.addComponent(new DetailData(details));
        object.addComponent(new RendererWidgetCollector(List.of(
                PassType.BLOCK_OUTLINE
        )));
        object.addComponent(new RendererHolder(List.of(
                new BlockOutlineData()
        )));
        object.addComponent(new FinderState());
        object.addComponent(new ClickWithFinder());
        object.addComponent(new WalkOn());
        object.addComponent(new SendClue());
        var test = new ItemClue();
        test.setPos(new BlockPos(1, -60, 0));
        test.setFace(Direction.UP);
        test.setStack(new ItemStack(ModItemsRegistry.CLUE_FINDER_ITEM.get()));
        object.addComponent(test);
        return object;
    }

    public static ClueObject createManualClue() {
        return Assembler.createManualClue("default name", new ArrayList<>());
    }

    // ====== clue book =========
    // copy: meta(id name), source(switch case), detail with completeness(switch case)
    private static ClueObject createClueBookClueWithoutSource(ClueObject old) {
        ClueObject copy = new ClueObject(old);
        // 1. copy meta
        copy.addComponent(new InfoData(old.getComponent(ComponentType.INFO_DATA)));
        // 2. allow sharing behavior
        copy.addComponent(new SendClue());
        // 2. generate details
        DetailWithCompleteness dCompo = switch (old.type()) {
            case MANUAL -> manualGenerate(old);
            case null, default ->
                    throw new RuntimeException("Unimplemented ClueBook detail component converter of ClueObject#" + old.getId());
        };
        copy.addComponent(dCompo);

        return copy;
    }

    public static ClueObject createClueBookClue(ClueObject old, BlockPos pos) {
        ClueObject copy = createClueBookClueWithoutSource(old);
        // 3. set source
        var sCompo = new FoundSource();
        sCompo.setSource(pos);
        copy.addComponent(sCompo);
        return copy;
    }

    public static ClueObject createClueBookClue(ClueObject old, Entity entity) {
        ClueObject copy = createClueBookClueWithoutSource(old);
        // 3. set source
        var sCompo = new FoundSource();
        sCompo.setSource(entity);
        copy.addComponent(sCompo);
        return copy;
    }

    public static ClueObject createClueBookClue(ClueObject old, Player player) {
        ClueObject copy = createClueBookClueWithoutSource(old);
        // 3. set source
        var sCompo = new FoundSource();
        sCompo.setSource(player);
        copy.addComponent(sCompo);
        return copy;
    }


    private static DetailWithCompleteness manualGenerate(ClueObject old) {
        var result = new DetailWithCompleteness();

        DetailData dCompo = old.getComponent(ComponentType.DETAIL_DATA);
        if (dCompo == null)
            throw new RuntimeException("Invalid manual ClueObject: has no detail component, id#" + old.getId());
        for (String s : dCompo.getDetails()) {
            result.add(s, 1f);
        }
        return result;
    }


}
