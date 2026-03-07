package io.github.daxigua2333.mocai_clues.component.data.discovery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.EnumSelectorComponent;
import io.github.daxigua2333.mocai_clues.guis.widget.editable.EditBoxWithBacking;
import io.github.daxigua2333.mocai_clues.guis.widget.editable.StringListWidget;
import io.github.daxigua2333.mocai_clues.utils.EnumCodecProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class InteractState extends EnumSelectorComponent<InteractState.StateType> {
    public enum StateType {
        REMAINING,
        ALLOWED_PLAYERS,
        ;

        public static final Codec<StateType> CODEC = EnumCodecProvider.createCodec(StateType.class);
    }

    private int remaining;
    private List<String> allowedPlayers;

    private InteractState(EnumSet<StateType> allowed, EnumSet<StateType> enabled, int remaining, List<String> allowedPlayers) {
        super(StateType.class, allowed, enabled);
        this.remaining = remaining;
        this.allowedPlayers = allowedPlayers;
    }

    private InteractState(EnumSet<StateType> allowed, EnumSet<StateType> enabled) {
        this(allowed, enabled, 1, new ArrayList<>());
    }

    public InteractState(EnumSet<StateType> allowed) {
        this(allowed, EnumSet.noneOf(StateType.class));
    }


    public int getRemaining() {
        return remaining;
    }

    public List<String> getAllowedPlayers() {
        return allowedPlayers;
    }

    // ============= logic ================
    public boolean isAccessible(Player player) {
        for (StateType type : enabled) {
            switch (type) {
                case REMAINING -> {
                    if (remaining <= 0) return false;
                }
                case ALLOWED_PLAYERS -> {
                    if (!allowedPlayers.contains(player.getScoreboardName())) return false;
                }
            }
        }
        return true;
    }

    public void onFound() {
        // decrease remaining
        if (isEnabled(StateType.REMAINING) && remaining > 0) {
            remaining--;
        }
    }

    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_STATE;
    }

    public static final Codec<InteractState> CODEC = RecordCodecBuilder.create(inst -> {
        Codec<EnumSet<StateType>> setCodec = EnumSelectorComponent.getSetCodec(StateType.class, StateType.CODEC);
        return EnumSelectorComponent.commonFields(inst, setCodec)
                .and(inst.group(
                        Codec.INT.fieldOf("remaining").forGetter(InteractState::getRemaining),
                        Codec.STRING.listOf().fieldOf("allowedPlayers").forGetter(InteractState::getAllowedPlayers)
                ))
                .apply(inst, InteractState::new);
    });

    @Override
    protected String getHeaderKey() {
        return MoCaiClues.MODID + ".screen.interact_state";
    }

    @Override
    protected List<AbstractWidget> getEditableWidget(StateType type) {
        var stringList = new StringListWidget(Minecraft.getInstance().font, 0, 0, 100, 100, this.allowedPlayers);
        stringList.setChangeListener(list -> this.allowedPlayers = list);

        return switch (type) {
            case REMAINING -> List.of(EditBoxWithBacking.intBox(0, 0, 100, 20,
                    () -> this.remaining, i -> this.remaining = i,
                    v -> true,
                    Component.literal("String")));
            case ALLOWED_PLAYERS -> List.of(stringList);
        };
    }
}
