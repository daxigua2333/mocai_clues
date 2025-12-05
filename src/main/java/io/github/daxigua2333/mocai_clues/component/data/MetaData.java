package io.github.daxigua2333.mocai_clues.component.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ClueType;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import net.minecraft.core.UUIDUtil;

import java.util.UUID;

public class MetaData extends ClueComponent {
    private final UUID id;
    private final String name;

    // ==== inherit ====
    @Override
    public ComponentType type() {
        return ComponentType.META_DATA;
    }

    // ==== constructors ====
    private MetaData(UUID id, String name) {
        super();
        this.id = id;
        this.name = name;
    }
    public MetaData(String name) {
        this(UUID.randomUUID(), name);
    }
    public MetaData() {
        this(UUID.randomUUID(), "default name");
    }

    // ==== codec ====
    public static final Codec<MetaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(MetaData::getId),
            Codec.STRING.fieldOf("name").forGetter(MetaData::getName)
    ).apply(instance, MetaData::new));

    // ==== getters ====
    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
}
