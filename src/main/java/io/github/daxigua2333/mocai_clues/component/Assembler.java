package io.github.daxigua2333.mocai_clues.component;

import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.component.data.ItemClue;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractEntry;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractPassiveBehavior;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractResult;
import io.github.daxigua2333.mocai_clues.component.world.finder.DetailWithCompleteness;
import io.github.daxigua2333.mocai_clues.component.world.finder.FinderState;
import io.github.daxigua2333.mocai_clues.component.world.finder.FoundSource;
import io.github.daxigua2333.mocai_clues.component.world.finder.SendClue;
import io.github.daxigua2333.mocai_clues.component.world.renderer.PassType;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.component.world.renderer.RendererWidgetCollector;
import io.github.daxigua2333.mocai_clues.component.world.renderer.data.BlockOutlineData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

// TODO: combine with FamilyRegistry
public class Assembler {
    public static ClueObject createDefaultByType(ClueType type) {
        return switch (type) {
            case MANUAL -> createManualClue();
            case ITEM -> createItemClue();
            case FOOTPRINT -> null;
        };
    }

    // ===== manual clue =====
    private static ClueObject createManualClue(String name, List<String> details) {
        ClueObject obj = new ClueObject(ClueType.MANUAL);
        obj.addComponent(new InfoData(name));
        obj.addComponent(new DetailData(details));

        obj.addComponent(new RendererWidgetCollector(List.of(
                PassType.BLOCK_OUTLINE
        )));
        obj.addComponent(new RendererHolder(List.of(
                new BlockOutlineData()
        )));

        obj.addComponent(new FinderState(true));
        obj.addComponent(new InteractEntry(InteractEntry.EntryType.CLICK_WITH_FINDER));
        obj.addComponent(new InteractResult(InteractResult.ResultType.SEND_MANUAL_CLUE));
        obj.addComponent(new InteractPassiveBehavior(InteractPassiveBehavior.BehaviorType.FLASH_DOT));

        return obj;
    }

    public static ClueObject createManualClue() {
        return Assembler.createManualClue("default name", new ArrayList<>());
    }


    public static ClueObject createItemClue(String name, ItemStack stack) {
        ClueObject obj = new ClueObject(ClueType.ITEM);
        obj.addComponent(new InfoData(name));
        obj.addComponent(new ItemClue(stack));

        obj.addComponent(new RendererWidgetCollector(List.of(
                PassType.BLOCK_OUTLINE
        )));
        obj.addComponent(new RendererHolder(List.of(
                new BlockOutlineData()
        )));

        obj.addComponent(new FinderState(true));
        obj.addComponent(new InteractEntry(InteractEntry.EntryType.CLICK_WITH_FINDER));
        obj.addComponent(new InteractResult(InteractResult.ResultType.SEND_ITEM));
        obj.addComponent(new InteractPassiveBehavior(InteractPassiveBehavior.BehaviorType.ITEM_RENDERER));

        return obj;
    }

    public static ClueObject createItemClue() {
        return createItemClue("default name", ItemStack.EMPTY);
    }


    // ====== clue book =========
    // copy: meta(id name), source(switch case), detail with completeness(switch case)
    public static ClueObject createClueBookClueWithoutSource(ClueObject old) {
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

    // TODO: route issue again....
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
