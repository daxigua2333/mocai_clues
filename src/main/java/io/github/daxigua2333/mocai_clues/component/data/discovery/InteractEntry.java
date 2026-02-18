package io.github.daxigua2333.mocai_clues.component.data.discovery;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.EnumSelectorComponent;
import io.github.daxigua2333.mocai_clues.utils.EnumCodecProvider;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.EnumSet;
import java.util.List;

public class InteractEntry extends EnumSelectorComponent<InteractEntry.EntryType> {
    public enum EntryType {
        WALK_ON,
        CLICK_WITH_FINDER,
//        CLICK_BLOCK_WITH_FINDER,
//        CLICK_ENTITY_WITH_FINDER,
        ;

        public static final Codec<EntryType> CODEC = EnumCodecProvider.createCodec(EntryType.class);
    }


    private InteractEntry(EnumSet<EntryType> allowed, EnumSet<EntryType> enabled) {
        super(EntryType.class, allowed, enabled);
    }

    /**
     * Default init must specify `allowed`
     */
    public InteractEntry(EnumSet<EntryType> allowed) {
        this(allowed, EnumSet.noneOf(EntryType.class));
    }

    /**
     * Simplified init of Single Entry
     */
    public InteractEntry(EntryType type) {
        this(EnumSet.of(type));
        enable(type);
    }

    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_ENTRY;
    }

    public static final Codec<InteractEntry> CODEC = createCodec(EntryType.class, EntryType.CODEC, InteractEntry::new);

    @Override
    protected String getHeaderKey() {
        return MoCaiClues.MODID + ".screen.interact_entry";
    }

    @Override
    protected List<AbstractWidget> getEditableWidget(EntryType type) {
        return List.of();
    }
}
