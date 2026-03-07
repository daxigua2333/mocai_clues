package io.github.daxigua2333.mocai_clues.component;

import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.data.InfoData;
import io.github.daxigua2333.mocai_clues.component.data.ItemClue;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractEntry;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractPassiveBehavior;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractResult;
import io.github.daxigua2333.mocai_clues.component.data.discovery.InteractState;
import io.github.daxigua2333.mocai_clues.component.data.BlockPosWithFace;
import io.github.daxigua2333.mocai_clues.component.data.discovery.cluebook.DetailWithCompleteness;
import io.github.daxigua2333.mocai_clues.component.data.renderer.RendererHolder;
import io.github.daxigua2333.mocai_clues.component.data.renderer.BlockOutlineData;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class Assembler {
    public static ClueObject createDefaultByType(ClueType type) {
        return switch (type) {
            case MANUAL -> createManualClue();
            case ITEM -> createItemClue();
            case FOOTPRINT -> throw new RuntimeException("Unimplemented");
            case CLUE_BOOK -> throw new RuntimeException("Can't create default Object of CLUE_BOOK type");
            default -> throw new RuntimeException("Unimplemented");
        };
    }

    // ===== manual clue =====
    private static ClueObject createManualClue(String name, List<String> details) {
        ClueObject obj = new ClueObject(ClueType.MANUAL);
        obj.addComponent(new InfoData(name));
        obj.addComponent(new DetailData(details));
        obj.addComponent(new BlockPosWithFace());

        obj.addComponent(new RendererHolder(List.of(
                new BlockOutlineData()
        )));

        obj.addComponent(new InteractState(EnumSet.of(InteractState.StateType.REMAINING, InteractState.StateType.ALLOWED_PLAYERS)));
        obj.addComponent(new InteractEntry(
                EnumSet.of(InteractEntry.EntryType.CLICK_WITH_FINDER, InteractEntry.EntryType.WALK_ON),
                EnumSet.of(InteractEntry.EntryType.CLICK_WITH_FINDER)));
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

        obj.addComponent(new RendererHolder(List.of(
                new BlockOutlineData()
        )));

        obj.addComponent(new InteractState(EnumSet.of(InteractState.StateType.REMAINING, InteractState.StateType.ALLOWED_PLAYERS)));
        obj.addComponent(new InteractEntry(InteractEntry.EntryType.CLICK_WITH_FINDER));
        obj.addComponent(new InteractResult(InteractResult.ResultType.SEND_ITEM));
        obj.addComponent(new InteractPassiveBehavior(InteractPassiveBehavior.BehaviorType.ITEM_RENDERER));

        return obj;
    }

    public static ClueObject createItemClue() {
        return createItemClue("default name", ItemStack.EMPTY);
    }


    // ====== clue book =========
    // copy: meta(id name), detail with completeness(switch case)
    public static ClueObject createClueBookClue(ClueObject old, DetailWithCompleteness dCompo) {
        ClueObject copy = new ClueObject(old, ClueType.CLUE_BOOK);
        copy.addComponent(new InfoData(old.getComponent(ComponentType.INFO_DATA)));
        copy.addComponent(dCompo);

        return copy;
    }

    public static ClueObject createClueBookClue(ClueObject old, UUID cluebookUUID, DetailWithCompleteness dCompo) {
        ClueObject copy = new ClueObject(cluebookUUID, ClueType.CLUE_BOOK);
        copy.addComponent(new InfoData(old.getComponent(ComponentType.INFO_DATA)));
        copy.addComponent(dCompo);
        return copy;
    }

}
