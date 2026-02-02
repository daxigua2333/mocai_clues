package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.guis.widget.editable.CollapsibleCheckbox;
import io.github.daxigua2333.mocai_clues.guis.widget.editable.EditBoxRow;
import io.github.daxigua2333.mocai_clues.guis.widget.editable.StringListWidget;
import io.github.daxigua2333.mocai_clues.guis.widget.uneditable.ScaledTextRow;
import io.github.daxigua2333.mocai_clues.guis.widget.uneditable.SplitLineRow;
import io.github.daxigua2333.mocai_clues.guis.widget.uneditable.TextListWithIndexRow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FinderState extends ClueComponent {
    private int remaining = 1;
    private List<String> allowedPlayers;
    private boolean doRenderFlashDot = true;
    private boolean doRestrictPlayers = false;
    private boolean doRestrictTimes = false;

    public FinderState(boolean doRenderFlashDot,
                       boolean doRestrictTimes, int remaining,
                       boolean doRestrictPlayers, List<String> allowedPlayers) {
        this.remaining = remaining;
        this.allowedPlayers = allowedPlayers;
        this.doRenderFlashDot = doRenderFlashDot;
        this.doRestrictPlayers = doRestrictPlayers;
        this.doRestrictTimes = doRestrictTimes;
    }

    public FinderState() {
        this(true, false, 1, false, new ArrayList<>());
    }

    private int getRemaining() {
        return remaining;
    }

    private List<String> getAllowedPlayers() {
        return allowedPlayers;
    }

    public boolean isDoRenderFlashDot() {
        return doRenderFlashDot;
    }

    public boolean isDoRestrictPlayers() {
        return doRestrictPlayers;
    }

    public boolean isDoRestrictTimes() {
        return doRestrictTimes;
    }

    public boolean isAccessible(String playerName) {
        if (doRestrictTimes && remaining <= 0) return false;
        if (doRestrictPlayers && !allowedPlayers.contains(playerName)) return false;
        return true;
    }

    private void decreaseRemaining() {
        if (remaining > 0) {
            remaining--;
        }
    }

    public void onFound() {
        decreaseRemaining();
    }

    @Override
    public ComponentType type() {
        return ComponentType.FINDER_STATE;
    }

//    public static final Codec<FinderState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.INT.fieldOf("remain").forGetter(FinderState::getRemain),
//            Codec.STRING.listOf().fieldOf("allowedPlayers").forGetter(FinderState::getAllowedPlayers)
//    ).apply(inst, FinderState::new));

//    public static final Codec<FinderState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            // int: missing => -1, encoding -1 => omit field
//            Codec.INT.optionalFieldOf("remain")
//                    .forGetter(d -> d.getRemain() == -1 ? Optional.empty() : Optional.of(d.getRemain())),
//            // list: missing => null, encoding null => omit field (empty list still serializes)
//            Codec.STRING.listOf().optionalFieldOf("allowedPlayers")
//                    .forGetter(d -> Optional.ofNullable(d.getAllowedPlayers())),
//            Codec.BOOL.optionalFieldOf("doRenderFlashDot").forGetter(d -> Optional.of(d.isDoRenderFlashDot()))
//    ).apply(inst, (intOpt, listOpt, boolOpt) ->
//            new FinderState(
//                    intOpt.orElse(-1),
//                    listOpt.orElse(null),
//                    boolOpt.orElse(true)
//            )
//    ));

    public static final Codec<FinderState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.BOOL.fieldOf("doRenderFlashDot").forGetter(FinderState::isDoRenderFlashDot),
            Codec.BOOL.fieldOf("doRestrictTimes").forGetter(FinderState::isDoRestrictTimes),
            Codec.INT.fieldOf("remaining").forGetter(FinderState::getRemaining),
            Codec.BOOL.fieldOf("doRestrictPlayers").forGetter(FinderState::isDoRestrictPlayers),
            Codec.STRING.listOf().fieldOf("allowedPlayers").forGetter(FinderState::getAllowedPlayers)
    ).apply(inst, FinderState::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        var stringList = new StringListWidget(Minecraft.getInstance().font, 0, 0, 100, 100, this.allowedPlayers);
        stringList.setChangeListener(list -> this.allowedPlayers = list);

        return List.of(
                new ScaledTextRow(0, 0, 100, 100, Component.translatable("FinderState"), 1.2f),  // TODO: translate
                new SplitLineRow(0, 0, 100, 4),
                // flash dot  TODO: tooltip of description
                new CollapsibleCheckbox(0, 0, 100, Component.translatable("doRenderFlashDot"),
                        doRenderFlashDot,
                        checked -> this.doRenderFlashDot = checked,
                        List.of()),
                // remaining
                new CollapsibleCheckbox(0, 0, 100, Component.translatable("remaining"),
                        doRestrictTimes,
                        checked -> this.doRestrictTimes = checked,
                        List.of(EditBoxRow.intBox(0, 0, 100, 20, 4, 4,
                                () -> this.remaining, i -> this.remaining = i,
                                v -> true,
                                Component.literal("String")))),
                // allowed players
                new CollapsibleCheckbox(0, 0, 100, Component.translatable("allowedPlayers"),
                        doRestrictPlayers,
                        checked -> this.doRestrictPlayers = checked,
                        List.of(stringList))
        );
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of(
                new ScaledTextRow(0, 0, 100, 100, Component.translatable("FinderState"), 1.2f),
                new SplitLineRow(0, 0, 100, 4),
                // flash dot
                new ScaledTextRow(0, 0, 100, 100, Component.translatable("doRenderFlashDot"), 1.1f),
                new ScaledTextRow(0, 0, 100, 100, Component.translatable(String.format("%b", doRenderFlashDot)), 1f),
                // remaining
                new ScaledTextRow(0, 0, 100, 100, Component.translatable("remaining"), 1.1f),
                new ScaledTextRow(0, 0, 100, 100, Component.literal(String.format("%d", remaining)), 1f),
                // allowed players
                new ScaledTextRow(0, 0, 100, 100, Component.translatable("allowedPlayers"), 1.1f),
                new TextListWithIndexRow(0, 0, 100, 100, allowedPlayers, 2)
        );
    }
}
