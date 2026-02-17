package io.github.daxigua2333.mocai_clues.component;

import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.component.data.ItemClue;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractEntry;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractPassiveBehavior;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractResult;
import io.github.daxigua2333.mocai_clues.component.world.data.BlockPosWithFace;
import io.github.daxigua2333.mocai_clues.component.world.finder.DetailWithCompleteness;
import io.github.daxigua2333.mocai_clues.component.world.finder.FinderState;
import io.github.daxigua2333.mocai_clues.component.world.finder.FoundSource;
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
            case FOOTPRINT -> throw new RuntimeException("Unimplemented");
            case CLUE_BOOK -> throw new RuntimeException("Can't create default Object of CLUE_BOOK type");
        };
    }

    // ===== manual clue =====
    private static ClueObject createManualClue(String name, List<String> details) {
        ClueObject obj = new ClueObject(ClueType.MANUAL);
        obj.addComponent(new InfoData(name));
        obj.addComponent(new DetailData(details));
        obj.addComponent(new BlockPosWithFace());

        obj.addComponent(new RendererWidgetCollector(List.of(
                PassType.BLOCK_OUTLINE
        )));
        obj.addComponent(new RendererHolder(List.of(
                new BlockOutlineData()
        )));

        obj.addComponent(new FinderState(true));
        obj.addComponent(new InteractEntry(InteractEntry.EntryType.CLICK_WITH_FINDER));
        obj.addComponent(new InteractResult(InteractResult.ResultType.SEND_TO_CLUE_BOOK));
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
        obj.addComponent(new BlockPosWithFace());

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
    public static ClueObject createClueBookClueWithoutSource(ClueObject old, DetailWithCompleteness dCompo) {
        ClueObject copy = new ClueObject(old, ClueType.CLUE_BOOK);
        copy.addComponent(new InfoData(old.getComponent(ComponentType.INFO_DATA)));
        copy.addComponent(dCompo);

        return copy;
    }

    // TODO: route issue again....
    public static ClueObject createClueBookClue(ClueObject old, BlockPos pos, DetailWithCompleteness dCompo) {
        ClueObject copy = createClueBookClueWithoutSource(old, dCompo);
        // 3. set source
        var sCompo = new FoundSource();
        sCompo.setSource(pos);
        copy.addComponent(sCompo);
        return copy;
    }

    public static ClueObject createClueBookClue(ClueObject old, Entity entity, DetailWithCompleteness dCompo) {
        ClueObject copy = createClueBookClueWithoutSource(old, dCompo);
        // 3. set source
        var sCompo = new FoundSource();
        sCompo.setSource(entity);
        copy.addComponent(sCompo);
        return copy;
    }

    public static ClueObject createClueBookClue(ClueObject old, Player player, DetailWithCompleteness dCompo) {
        ClueObject copy = createClueBookClueWithoutSource(old, dCompo);
        // 3. set source
        var sCompo = new FoundSource();
        sCompo.setSource(player);
        copy.addComponent(sCompo);
        return copy;
    }

}
