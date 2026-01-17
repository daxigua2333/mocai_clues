package io.github.daxigua2333.mocai_clues.component.world.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.UUIDUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AttachedEntitySet extends ClueComponent {
    private final Set<UUID> set;

    private AttachedEntitySet(Set<UUID> set) {
        this.set = new HashSet<>(set);
    }
    public AttachedEntitySet() {
        this(Set.of());
    }

    // TODO: entity lifecycle
    public void add(UUID id) {
        set.add(id);
    }
    public Set<UUID> getImmutable() {
        return Collections.unmodifiableSet(set);
    }
    private HashSet<UUID> getSet() {
        return new HashSet<>(set);
    }

    @Override
    public ComponentType type() {
        return ComponentType.ATTACHED_ENTITY_SET;
    }

    public static final Codec<AttachedEntitySet> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.listOf().xmap(HashSet::new, List::copyOf).fieldOf("set").forGetter(AttachedEntitySet::getSet)
    ).apply(inst, AttachedEntitySet::new));

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
