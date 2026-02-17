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

public class InteractEntry extends ClueComponent {
    public enum EntryType {
        WALK_ON,
        CLICK_WITH_FINDER,
//        CLICK_BLOCK_WITH_FINDER,
//        CLICK_ENTITY_WITH_FINDER,
        ;

        public static final Codec<EntryType> CODEC = EnumCodecProvider.createCodec(EntryType.class);
    }

    private final EnumSet<EntryType> enabled;

    private InteractEntry(EnumSet<EntryType> enabled) {
        this.enabled = enabled;
    }

    public InteractEntry() {
        this(EnumSet.noneOf(EntryType.class));
    }

    public InteractEntry(EntryType type) {
        this();
        this.setSingleEnabled(type);
    }

    private EnumSet<EntryType> getEnabled() {
        return enabled;
    }

    public void setSingleEnabled(EntryType type) {
        enabled.clear();
        enabled.add(type);
    }

    public boolean hasEntry(EntryType type) {
        return enabled.contains(type);
    }

    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_ENTRY;
    }

    public static final Codec<InteractEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntryType.CODEC.listOf().xmap(
                    list -> list.isEmpty() ? EnumSet.noneOf(EntryType.class) : EnumSet.copyOf(list),
                    set -> List.copyOf(set)
            ).fieldOf("set").forGetter(InteractEntry::getEnabled)
    ).apply(inst, InteractEntry::new));


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
