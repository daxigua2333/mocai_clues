package io.github.daxigua2333.mocai_clues.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * should contain fields: UUID, type
 * component should implement: type() constructor codec(Codec.unit if empty) editable
 * */
public class ClueObject {
    private final UUID id;
    private final ClueType type;
    // TODO: use List + index Map to allow multi components and fast lookup
    private final Map<ComponentType, ClueComponent> components;

    // ==== constructor(codec part) ====
    private ClueObject(UUID id, ClueType type, Map<ComponentType, ClueComponent> map) {
        this.id = id;
        this.type =type;
        this.components = new EnumMap<>(map);
        for (var compo : components.values()) {
            compo.setOwner(this);
        }
    }
    public ClueObject(ClueType type) {
        this(
                UUID.randomUUID(),
                type,
                new EnumMap<>(ComponentType.class)
        );
    }


    // ===== getter setter ====
    public UUID getId() {
        return id;
    }
    public ClueType type() {
        return type;
    }
    // for codec
    private Map<ComponentType, ClueComponent> getMap() {return components;}


    // ==== map apis ====
    public void addComponent(ClueComponent component) {
        component.setOwner(this);
        components.put(component.type(), component);
//        component.onAdded(this);
    }

    public void removeComponent(ComponentType type) {
        components.remove(type);
    }

    public boolean hasComponent(ComponentType type) {
        return components.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends ClueComponent> T getComponent(ComponentType type) {
        return (T) components.get(type);
    }

    public Collection<ClueComponent> getComponents() {
        return components.values();
    }

    // ==== Codec ====
//    public static final Codec<ClueObject> CODEC = MAP_CODEC.xmap(ClueObject::new, ClueObject::getMap);
    public static final Codec<ClueObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(ClueObject::getId),
            ClueType.CODEC.fieldOf("type").forGetter(ClueObject::type),
            ComponentType.MAP_CODEC.fieldOf("components").forGetter(ClueObject::getMap)
    ).apply(instance, ClueObject::new));

    // NBT
    public CompoundTag toNbt() {
        DynamicOps<Tag> ops = NbtOps.INSTANCE;
        DataResult<Tag> result = CODEC.encodeStart(ops, this);
        return result.result()
                .map(tag -> (CompoundTag) tag) // assuming your codec encodes to a CompoundTag
                .orElseGet(() -> {
                    // handle errors however you like
                    throw new IllegalStateException("Failed to encode MyData to NBT: " +
                            result.error().map(e -> e.message()).orElse("unknown"));
                });
    }
    public static ClueObject fromNbt(CompoundTag tag) {
        DynamicOps<Tag> ops = NbtOps.INSTANCE;
        DataResult<ClueObject> result = CODEC.parse(ops, tag);
        return result.getOrThrow(msg -> {
            throw new IllegalStateException("Failed to decode MyData from NBT: " + msg);
        });
    }

    // stream codec: reuse codec  TODO: optimize packet size
    public static final StreamCodec<ByteBuf, ClueObject> STREAM_CODEC =
//        ByteBufCodecs.fromCodecWithRegistries(ClueObject.CODEC);
        ByteBufCodecs.fromCodec(ClueObject.CODEC);


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClueObject other)) return false;

        // Codec-defined equality:
        return this.toNbt().equals(other.toNbt());
    }

    @Override
    public int hashCode() {
        // Must match equals(): use the same representation
        return toNbt().hashCode();
    }

    @Override
    public ClueObject clone() {
        return ClueObject.fromNbt(this.toNbt());
    }

//    public void update(float deltaTime) {
//        if (!active) return;
//        for (Component c : components.values()) {
//            c.update(deltaTime);
//        }
//    }
//    private boolean active = true;
//    public boolean isActive() {
//        return active;
//    }
//    public void setActive(boolean active) {
//        this.active = active;
//    }


}
