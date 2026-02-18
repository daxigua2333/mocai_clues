package io.github.daxigua2333.mocai_clues.component.data.discovery;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.EnumSelectorComponent;
import io.github.daxigua2333.mocai_clues.utils.EnumCodecProvider;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.EnumSet;
import java.util.List;

public class InteractPassiveBehavior extends EnumSelectorComponent<InteractPassiveBehavior.BehaviorType> {
    public enum BehaviorType {
        FLASH_DOT,
        ITEM_RENDERER,
        ;

        public static final Codec<BehaviorType> CODEC = EnumCodecProvider.createCodec(BehaviorType.class);
    }


    private InteractPassiveBehavior(EnumSet<BehaviorType> allowed, EnumSet<BehaviorType> enabled) {
        super(BehaviorType.class, allowed, enabled);
    }

    /**
     * Default init must specify `allowed`
     */
    public InteractPassiveBehavior(EnumSet<BehaviorType> allowed) {
        this(allowed, EnumSet.noneOf(BehaviorType.class));
    }

    /**
     * Simplified init of Single Behavior
     */
    public InteractPassiveBehavior(BehaviorType type) {
        this(EnumSet.of(type));
        enable(type);
    }

    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_PASSIVE_BEHAVIOR;
    }

    public static final Codec<InteractPassiveBehavior> CODEC = createCodec(BehaviorType.class, BehaviorType.CODEC, InteractPassiveBehavior::new);

    @Override
    protected String getHeaderKey() {
        return MoCaiClues.MODID + ".screen.passive_behavior";
    }

    @Override
    protected List<AbstractWidget> getEditableWidget(BehaviorType type) {
        return List.of();
    }
}
