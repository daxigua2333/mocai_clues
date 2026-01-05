package io.github.daxigua2333.mocai_clues.component.world.finder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FinderState extends ClueComponent {
    // these default value means no behavior
    private int remain = -1;  // only
    private List<String> allowedPlayers = null;

    public FinderState(int remain, List<String> allowedPlayers) {
        this.remain = remain;
        this.allowedPlayers = allowedPlayers;
    }
    public FinderState() {
        this(-1, null);
    }

    private int getRemain() {
        return remain;
    }
    private List<String> getAllowedPlayers() {
        return allowedPlayers;
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
                    .forGetter(d -> Optional.ofNullable(d.getAllowedPlayers()))
    ).apply(inst, (intOpt, listOpt) ->
            new FinderState(
                    intOpt.orElse(-1),
                    listOpt.orElse(null)
            )
    ));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable() {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of();
    }
}
