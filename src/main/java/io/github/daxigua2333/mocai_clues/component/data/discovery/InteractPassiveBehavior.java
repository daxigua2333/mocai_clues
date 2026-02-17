package io.github.daxigua2333.mocai_clues.component.data.discovery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.utils.EnumCodecProvider;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class InteractPassiveBehavior extends ClueComponent {
    public enum BehaviorType {
        FLASH_DOT,
        ITEM_RENDERER,
        ;

        public static final Codec<BehaviorType> CODEC = EnumCodecProvider.createCodec(BehaviorType.class);
    }

    private final EnumSet<BehaviorType> enabled;

    public InteractPassiveBehavior(EnumSet<BehaviorType> enabled) {
        this.enabled = enabled;
    }

    public InteractPassiveBehavior() {
        this(EnumSet.noneOf(BehaviorType.class));
    }

    public InteractPassiveBehavior(BehaviorType type) {
        this();
        addBehavior(type);
    }

    public EnumSet<BehaviorType> getEnabled() {
        return enabled;
    }

    public boolean hasBehavior(BehaviorType type) {
        return enabled.contains(type);
    }

    public void addBehavior(BehaviorType type) {
        enabled.add(type);
    }


    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_PASSIVE_BEHAVIOR;
    }

    public static final Codec<InteractPassiveBehavior> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BehaviorType.CODEC.listOf().xmap(
                    list -> list.isEmpty() ? EnumSet.noneOf(BehaviorType.class) : EnumSet.copyOf(list),
                    set -> List.copyOf(set)
            ).fieldOf("set").forGetter(InteractPassiveBehavior::getEnabled)
    ).apply(inst, InteractPassiveBehavior::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of();
    }
}
