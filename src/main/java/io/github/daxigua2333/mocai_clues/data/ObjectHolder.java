package io.github.daxigua2333.mocai_clues.data;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;
import java.util.function.Function;

/**
 * A LinkedHashMap<UUID, T> wrapper with:
 *  - normal map-like APIs
 *  - full codec & stream codec
 *  - delta tracking for AttachmentSyncHandler
 */
public final class ObjectHolder<T> {

    private final LinkedHashMap<UUID, T> backing = new LinkedHashMap<>();

    private final Function<T, UUID> idGetter;
    private final Codec<T> elementCodec;
    private final StreamCodec<ByteBuf, T> elementStreamCodec;

    // Delta tracking
    private final Set<UUID> dirty = new LinkedHashSet<>();
    private final Set<UUID> removed = new LinkedHashSet<>();
    private boolean cleared;

    public ObjectHolder(Function<T, UUID> idGetter,
                        Codec<T> elementCodec,
                        StreamCodec<ByteBuf, T> elementStreamCodec) {
        this.idGetter = Objects.requireNonNull(idGetter);
        this.elementCodec = Objects.requireNonNull(elementCodec);
        this.elementStreamCodec = Objects.requireNonNull(elementStreamCodec);
    }

    private ObjectHolder(Function<T, UUID> idGetter,
                         Codec<T> elementCodec,
                         StreamCodec<ByteBuf, T> elementStreamCodec,
                         Map<UUID, T> initialValues) {
        this(idGetter, elementCodec, elementStreamCodec);
        this.backing.putAll(initialValues);
    }

    // ---------------------------------------------------------------------
    // Basic Map-like API
    // ---------------------------------------------------------------------

    public int size() {
        return backing.size();
    }

    public boolean isEmpty() {
        return backing.isEmpty();
    }

    public boolean containsKey(UUID id) {
        return backing.containsKey(id);
    }

    public T get(UUID id) {
        return backing.get(id);
    }

    public Collection<T> values() {
        return Collections.unmodifiableCollection(backing.values());
    }

    public Set<UUID> keySet() {
        return Collections.unmodifiableSet(backing.keySet());
    }

    public Map<UUID, T> asMapView() {
        return Collections.unmodifiableMap(backing);
    }

    /** Put using the UUID from T itself. */
    public T put(T value) {
        UUID id = idGetter.apply(value);
        return put(id, value);
    }

    /** Put with explicit UUID key (still assumed to match T’s own UUID). */
    public T put(UUID id, T value) {
        T previous = backing.put(id, value);
        markAddedOrUpdated(id);
        return previous;
    }

    public T remove(UUID id) {
        T previous = backing.remove(id);
        if (previous != null) {
            markRemoved(id);
        }
        return previous;
    }

    public void clear() {
        if (!backing.isEmpty()) {
            backing.clear();
            markCleared();
        }
    }

    /**
     * Call this if you mutate an existing T in-place without going through put().
     */
    public void markDirty(T value) {
        UUID id = idGetter.apply(value);
        if (backing.containsKey(id)) {
            markAddedOrUpdated(id);
        }
    }

    // ---------------------------------------------------------------------
    // Change tracking for deltas
    // ---------------------------------------------------------------------

    private void markAddedOrUpdated(UUID id) {
        dirty.add(id);
        removed.remove(id);
    }

    private void markRemoved(UUID id) {
        removed.add(id);
        dirty.remove(id);
    }

    private void markCleared() {
        cleared = true;
        dirty.clear();
        removed.clear();
    }

    public boolean hasChanges() {
        return cleared || !dirty.isEmpty() || !removed.isEmpty();
    }

    public void resetChangeTracking() {
        cleared = false;
        dirty.clear();
        removed.clear();
    }

    // ---------------------------------------------------------------------
    // Full encode/decode (for persistence or full sync)
    // ---------------------------------------------------------------------

    /**
     * Full binary encode with the element StreamCodec.
     * Layout: varInt size, then that many T elements.
     */
    public void encodeFull(ByteBuf buf) {
        buf.writeInt(backing.size());
        for (T value : backing.values()) {
            elementStreamCodec.encode(buf, value);
        }
    }

    /**
     * Full binary decode with the element StreamCodec.
     */
    public void decodeFull(ByteBuf buf) {
        backing.clear();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            T value = elementStreamCodec.decode(buf);
            backing.put(idGetter.apply(value), value);
        }
        resetChangeTracking();
    }

    // ---------------------------------------------------------------------
    // Delta encode/decode (for AttachmentSyncHandler)
    // ---------------------------------------------------------------------
    // NOTE: this assumes the client already has a previous map state.
    // If used for the very first sync, you should send full instead.

    /**
     * Encode only changes since last resetChangeTracking().
     *
     * Layout:
     *  - boolean cleared
     *  - varInt changedCount, then changedCount * T
     *  - varInt removedCount, then removedCount * UUID
     *
     *  ** This is the same as delta payload buf
     */
    public void encodeDelta(ByteBuf buf) {
        buf.writeBoolean(cleared);

        buf.writeInt(dirty.size());
        for (UUID id : dirty) {
            T value = backing.get(id);
            if (value != null) {
                elementStreamCodec.encode(buf, value);
            }
        }

        buf.writeInt(removed.size());
        for (UUID id : removed) {
            UUIDUtil.STREAM_CODEC.encode(buf, id);
        }
    }

    /**
     * Apply a delta onto the existing map.
     * This mirrors encodeDelta's layout.
     */
    public void applyDelta(ByteBuf buf) {
        boolean cleared = buf.readBoolean();
        if (cleared) {
            backing.clear();
        }

        int changed = buf.readInt();
        for (int i = 0; i < changed; i++) {
            T value = elementStreamCodec.decode(buf);
            backing.put(idGetter.apply(value), value);
        }

        int removedCount = buf.readInt();
        for (int i = 0; i < removedCount; i++) {
            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
            backing.remove(id);
        }

        resetChangeTracking();
    }

    // ---------------------------------------------------------------------
    // Codec & StreamCodec factories
    // ---------------------------------------------------------------------
    // These are static so you can build the codecs where you register things.

    /**
     * Full Codec for persistence (JSON/NBT etc.). We just serialize as a list&lt;T&gt;.
     * T’s own codec is expected to include its UUID id.
     */
    public static <T> Codec<ObjectHolder<T>> codec(
            Function<T, UUID> idGetter,
            Codec<T> elementCodec,
            StreamCodec<ByteBuf, T> elementStreamCodec
    ) {
        // Persist as a simple list of elements; UUIDs come from the element itself.
        return elementCodec.listOf().xmap(
                list -> {
                    LinkedHashMap<UUID, T> map = new LinkedHashMap<>();
                    for (T value : list) {
                        map.put(idGetter.apply(value), value);
                    }
                    return new ObjectHolder<>(idGetter, elementCodec, elementStreamCodec, map);
                },
                registry -> List.copyOf(registry.backing.values())
        );
    }

    /**
     * Full StreamCodec (no deltas) suitable for custom packets, etc.
     * Deltas are handled by AttachmentSyncHandler instead.
     */
    public static <T> StreamCodec<ByteBuf, ObjectHolder<T>> streamCodec(
            Function<T, UUID> idGetter,
            Codec<T> elementCodec,
            StreamCodec<ByteBuf, T> elementStreamCodec
    ) {
        // Use ofMember so we can use the instance method encodeFull, as NeoForge suggests.
        return StreamCodec.ofMember(
                ObjectHolder<T>::encodeFull,
                buf -> {
                    ObjectHolder<T> map = new ObjectHolder<>(idGetter, elementCodec, elementStreamCodec);
                    map.decodeFull(buf);
                    return map;
                }
        );
    }

    /** this is delta payload part */
    public record DeltaPayload<T>(boolean cleared, List<T> dirty, List<UUID> removed) {
        public static <T> StreamCodec<ByteBuf, DeltaPayload<T>> deltaStreamCodec(
                StreamCodec<ByteBuf, T> elementStreamCodec
        ){
            return StreamCodec.of(
                    (ByteBuf buf, DeltaPayload<T> payload) -> {
                        buf.writeBoolean(payload.cleared());

                        buf.writeInt(payload.dirty().size());
                        for (T object : payload.dirty()) {
                            if (object != null) {
                                elementStreamCodec.encode(buf, object);
                            }
                        }

                        buf.writeInt(payload.removed().size());
                        for (UUID id : payload.removed()) {
                            UUIDUtil.STREAM_CODEC.encode(buf, id);
                        }
                    },
                    buf -> {
                        boolean cleared = buf.readBoolean();

                        List<T> dirty = new ArrayList<>();
                        int changed = buf.readInt();
                        for (int i=0; i<changed; i++) {
                            T obj = elementStreamCodec.decode(buf);
                            dirty.add(obj);
                        }

                        List<UUID> removed = new ArrayList<>();
                        int removedCount = buf.readInt();
                        for (int i=0; i<removedCount; i++) {
                            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
                            removed.add(id);
                        }

                        return new DeltaPayload<T>(
                                cleared,
                                dirty,
                                removed
                        );
                    }
            );

        }
    }

    public DeltaPayload<T> getDeltaPayload() {
        List<T> resultDirty = new ArrayList<>();
        for (UUID id : dirty) {
            resultDirty.add(backing.get(id));
        }

        return new DeltaPayload<>(
                cleared,
                resultDirty,
                new ArrayList<>(removed)
        );
    }

    public void applyDeltaPayload(DeltaPayload<T> payload) {
        if (payload.cleared()) {
            backing.clear();
        }

        for (var obj : payload.dirty()) {
            backing.put(idGetter.apply(obj), obj);
        }

        for (var id : payload.removed()) {
            backing.remove(id);
        }

        resetChangeTracking();
    }

    // TODO: index things  hook priority
    public void queryTypes(){

    }
}
