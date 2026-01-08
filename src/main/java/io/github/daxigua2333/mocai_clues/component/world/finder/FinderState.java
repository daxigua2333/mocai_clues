package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.ScaledTextRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.SplitLineRow;
import io.github.daxigua2333.mocai_clues.component.gui.uneditable.TextListWithIndexRow;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FinderState extends ClueComponent {
    // these default value means no behavior
    private int remain = -1;  // only
    private List<String> allowedPlayers = null;
    private boolean doRenderFlashDot = true;

    public FinderState(int remain, List<String> allowedPlayers, boolean doRenderFlashDot) {
        this.remain = remain;
        this.allowedPlayers = allowedPlayers;
        this.doRenderFlashDot = doRenderFlashDot;
    }
    public FinderState() {
        this(-1, null, true);
    }

    private int getRemain() {
        return remain;
    }
    private List<String> getAllowedPlayers() {
        return allowedPlayers;
    }
    private boolean getDoRenderFlashDot() {
        return doRenderFlashDot;
    }

    public boolean isAccessible(String playerName) {
        if (remain == 0) return false;
        if (allowedPlayers != null && !allowedPlayers.contains(playerName)) return false;
        return true;
    }

    private void decreaseRemain() {
        if (remain > 0) {
            remain--;
        }
    }

    public void onFound() {
        decreaseRemain();
    }

    @Override
    public ComponentType type() {
        return ComponentType.FINDER_STATE;
    }

//    public static final Codec<FinderState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
//            Codec.INT.fieldOf("remain").forGetter(FinderState::getRemain),
//            Codec.STRING.listOf().fieldOf("allowedPlayers").forGetter(FinderState::getAllowedPlayers)
//    ).apply(inst, FinderState::new));

    public static final Codec<FinderState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            // int: missing => -1, encoding -1 => omit field
            Codec.INT.optionalFieldOf("remain")
                    .forGetter(d -> d.getRemain() == -1 ? Optional.empty() : Optional.of(d.getRemain())),
            // list: missing => null, encoding null => omit field (empty list still serializes)
            Codec.STRING.listOf().optionalFieldOf("allowedPlayers")
                    .forGetter(d -> Optional.ofNullable(d.getAllowedPlayers())),
            Codec.BOOL.optionalFieldOf("doRenderFlashDot").forGetter(d -> Optional.of(d.getDoRenderFlashDot()))
    ).apply(inst, (intOpt, listOpt, boolOpt) ->
            new FinderState(
                    intOpt.orElse(-1),
                    listOpt.orElse(null),
                    boolOpt.orElse(true)
            )
    ));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable() {
        return List.of(); // TODO
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of(
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.translatable("FinderState"), 1.2f),
                new SplitLineRow(0, 0, 100, 100, 2, 2),
                // flash dot
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.translatable("doRenderFlashDot"), 1.1f),
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.translatable(String.format("%b", doRenderFlashDot)), 1f),
                // remaining
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.translatable("remaining"), 1.1f),
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.literal(String.format("%d", remain)), 1f),
                // allowed players
                new ScaledTextRow(0, 0, 100, 100, 2, 2, Component.translatable("allowedPlayers"), 1.1f),
                new TextListWithIndexRow(0, 0, 100, 100, 2, 2, allowedPlayers, 2)
        );
    }
}
