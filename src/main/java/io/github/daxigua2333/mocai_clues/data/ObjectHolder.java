package io.github.daxigua2333.mocai_clues.data;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * A LinkedHashMap<UUID, T> wrapper with:
 *  - normal map-like APIs
 *  - full codec & stream codec
 *  - delta tracking for AttachmentSyncHandler
 *  - derived secondary indexes (in-memory only, not serialized)
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

    // ---------------------------------------------------------------------
    // Secondary indexes (derived, not serialized)
    // ---------------------------------------------------------------------

    /** Derived secondary indexes, kept in sync with the backing map. Not serialized. */
    private final Map<String, Index<?>> indexes = new LinkedHashMap<>();

    public enum DuplicateKeyHandling {
        /** Throw when a unique index sees the same key mapped to a different UUID. */
        THROW,
        /** Keep the existing mapping; the new element will not be reachable via that key. */
        KEEP_EXISTING,
        /** Replace the existing mapping; the new element wins. */
        REPLACE_EXISTING
    }

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

//    /** type declare helper */
//    public <U> Optional<ObjectHolder<U>> asType(Class<U> targetType) {
//        var set = backing.values();
//        if (!set.isEmpty() && targetType.isInstance(set.toArray()[0])) {
//            return Optional.of((ObjectHolder<U>) this);
//        }
//        return Optional.empty();  // but even if its empty, it should have type
//    }

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
        UUID actualId = idGetter.apply(value);
        if (!id.equals(actualId)) {
            throw new IllegalArgumentException("put(UUID, T): key id " + id + " does not match value id " + actualId);
        }

        // Update indexes first so unique-index failures don't leave the backing map half-updated.
        if (!indexes.isEmpty()) {
            for (Index<?> index : indexes.values()) {
                index.onPut(id, value);
            }
        }

        T previous = backing.put(id, value);
        markAddedOrUpdated(id);
        return previous;
    }

    public T remove(UUID id) {
        T previous = backing.remove(id);
        if (previous != null) {
            if (!indexes.isEmpty()) {
                for (Index<?> index : indexes.values()) {
                    index.onRemove(id);
                }
            }
            markRemoved(id);
        }
        return previous;
    }

    public void clear() {
        if (!backing.isEmpty()) {
            backing.clear();
            if (!indexes.isEmpty()) {
                for (Index<?> index : indexes.values()) {
                    index.onClear();
                }
            }
            markCleared();
        }
    }

    /**
     * Call this if you mutate an existing T in-place without going through put().
     */
    public void markDirty(T value) {
        markDirty(idGetter.apply(value));
    }

    /**
     * Call this if you mutate an existing T in-place without going through put().
     * This will also re-index the entry for all registered indexes.
     */
    public void markDirty(UUID id) {
        if (!backing.containsKey(id)) {
            return;
        }

        if (!indexes.isEmpty()) {
            T current = backing.get(id);
            if (current != null) {
                for (Index<?> index : indexes.values()) {
                    index.onPut(id, current);
                }
            }
        }

        markAddedOrUpdated(id);
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
    // Index API
    // ---------------------------------------------------------------------

    /**
     * Create a (non-unique) index where each element contributes at most one key.
     * The index is derived-only (not serialized) and kept in sync automatically.
     */
    public <K> Index<K> createIndex(String name, Function<T, K> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor");
        return createIndexInternal(
                name,
                false,
                DuplicateKeyHandling.REPLACE_EXISTING,
                value -> {
                    K key = keyExtractor.apply(value);
                    return key == null ? List.of() : List.of(key);
                }
        );
    }

    /**
     * Create a UNIQUE index where each key should map to at most one UUID.
     *
     * <p>Default policy is {@link DuplicateKeyHandling#REPLACE_EXISTING} (last write wins),
     * to avoid crashing from bad/old data during sync/load.</p>
     */
    public <K> Index<K> createUniqueIndex(String name, Function<T, K> keyExtractor) {
        return createUniqueIndex(name, keyExtractor, DuplicateKeyHandling.REPLACE_EXISTING);
    }

    public <K> Index<K> createUniqueIndex(String name, Function<T, K> keyExtractor, DuplicateKeyHandling duplicateKeyHandling) {
        Objects.requireNonNull(keyExtractor, "keyExtractor");
        return createIndexInternal(
                name,
                true,
                duplicateKeyHandling,
                value -> {
                    K key = keyExtractor.apply(value);
                    return key == null ? List.of() : List.of(key);
                }
        );
    }

    /** Create a (non-unique) index where each element can contribute multiple keys (e.g., tags). */
    public <K> Index<K> createMultiIndex(String name, Function<T, ? extends Collection<K>> keysExtractor) {
        return createIndexInternal(name, false, DuplicateKeyHandling.REPLACE_EXISTING, keysExtractor);
    }

    /** Create a UNIQUE multi-key index. */
    public <K> Index<K> createUniqueMultiIndex(String name,
                                               Function<T, ? extends Collection<K>> keysExtractor,
                                               DuplicateKeyHandling duplicateKeyHandling) {
        return createIndexInternal(name, true, duplicateKeyHandling, keysExtractor);
    }

    private <K> Index<K> createIndexInternal(String name,
                                             boolean unique,
                                             DuplicateKeyHandling duplicateKeyHandling,
                                             Function<T, ? extends Collection<K>> keysExtractor) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(keysExtractor, "keysExtractor");
        Objects.requireNonNull(duplicateKeyHandling, "duplicateKeyHandling");

        if (indexes.containsKey(name)) {
            throw new IllegalArgumentException("Index already exists: " + name);
        }

        Index<K> index = new Index<>(name, unique, duplicateKeyHandling, keysExtractor);
        indexes.put(name, index);
        index.rebuild();
        return index;
    }

    public Set<String> indexNames() {
        return Collections.unmodifiableSet(indexes.keySet());
    }

    @Nullable
    public Index<?> getIndex(String name) {
        return indexes.get(name);
    }

    public void dropIndex(String name) {
        Index<?> removed = indexes.remove(name);
        if (removed != null) {
            removed.clear();
        }
    }

    /** Rebuild every registered index from the current backing map. */
    public void rebuildAllIndexes() {
        for (Index<?> index : indexes.values()) {
            index.rebuild();
        }
    }

    public final class Index<K> {
        private final String name;
        private final boolean unique;
        private final DuplicateKeyHandling duplicateKeyHandling;
        private final Function<T, ? extends Collection<K>> keysExtractor;

        // key -> ids (multiple ids for non-unique; for unique should be 0/1 ids)
        private final Map<K, LinkedHashSet<UUID>> idsByKey = new LinkedHashMap<>();

        // id -> keys (reverse mapping, for efficient updates/removals)
        private final Map<UUID, LinkedHashSet<K>> keysById = new HashMap<>();

        private Index(String name,
                      boolean unique,
                      DuplicateKeyHandling duplicateKeyHandling,
                      Function<T, ? extends Collection<K>> keysExtractor) {
            this.name = name;
            this.unique = unique;
            this.duplicateKeyHandling = duplicateKeyHandling;
            this.keysExtractor = keysExtractor;
        }

        public String name() {
            return name;
        }

        public boolean unique() {
            return unique;
        }

        public DuplicateKeyHandling duplicateKeyHandling() {
            return duplicateKeyHandling;
        }

        /** All indexed keys (live view). */
        public Set<K> keySet() {
            return Collections.unmodifiableSet(idsByKey.keySet());
        }

        /** UUIDs that match this key (may be empty). */
        public Set<UUID> ids(K key) {
            LinkedHashSet<UUID> ids = idsByKey.get(key);
            return ids == null ? Set.of() : Collections.unmodifiableSet(ids);
        }

        /** Values that match this key (may be empty). */
        public List<T> values(K key) {
            LinkedHashSet<UUID> ids = idsByKey.get(key);
            if (ids == null || ids.isEmpty()) {
                return List.of();
            }
            ArrayList<T> result = new ArrayList<>(ids.size());
            for (UUID id : ids) {
                T value = backing.get(id);
                if (value != null) {
                    result.add(value);
                }
            }
            return List.copyOf(result);
        }

        /** The keys this UUID is currently indexed under (may be empty). */
        public Set<K> keys(UUID id) {
            LinkedHashSet<K> keys = keysById.get(id);
            return keys == null ? Set.of() : Collections.unmodifiableSet(keys);
        }

        public void clear() {
            idsByKey.clear();
            keysById.clear();
        }

        public void rebuild() {
            clear();
            for (var entry : backing.entrySet()) {
                onPut(entry.getKey(), entry.getValue());
            }
        }

        private void onClear() {
            clear();
        }

        private void onRemove(UUID id) {
            LinkedHashSet<K> keys = keysById.remove(id);
            if (keys == null) {
                return;
            }
            for (K key : keys) {
                unlink(key, id);
            }
        }

        private void onPut(UUID id, T value) {
            LinkedHashSet<K> desiredKeys = extractKeys(value);
            LinkedHashSet<K> oldKeys = keysById.get(id);

            // No changes
            if (oldKeys != null && oldKeys.equals(desiredKeys)) {
                return;
            }

            // Remove old keys that are no longer present
            if (oldKeys != null) {
                for (K oldKey : oldKeys) {
                    if (!desiredKeys.contains(oldKey)) {
                        unlink(oldKey, id);
                    }
                }
            }

            // Add new keys (respect unique policy)
            LinkedHashSet<K> actualKeys = new LinkedHashSet<>();
            for (K key : desiredKeys) {
                if (link(key, id)) {
                    actualKeys.add(key);
                }
            }

            if (actualKeys.isEmpty()) {
                keysById.remove(id);
            } else {
                keysById.put(id, actualKeys);
            }
        }

        private LinkedHashSet<K> extractKeys(T value) {
            Collection<K> raw = keysExtractor.apply(value);
            if (raw == null || raw.isEmpty()) {
                return new LinkedHashSet<>();
            }
            LinkedHashSet<K> keys = new LinkedHashSet<>();
            for (K key : raw) {
                if (key != null) {
                    keys.add(key);
                }
            }
            return keys;
        }

        /**
         * @return true if the key was linked to this id; false if skipped (KEEP_EXISTING case on unique conflict)
         */
        private boolean link(K key, UUID id) {
            if (key == null) {
                return false;
            }

            LinkedHashSet<UUID> ids = idsByKey.computeIfAbsent(key, k -> new LinkedHashSet<>());
            if (ids.contains(id)) {
                return true;
            }

            // Non-unique (or unique and currently empty): just add.
            if (!unique || ids.isEmpty()) {
                ids.add(id);
                return true;
            }

            // Unique conflict: key already mapped to a different id.
            return switch (duplicateKeyHandling) {
                case THROW -> throw new IllegalStateException(
                        "Unique index '" + name + "' duplicate key '" + key + "' for id=" + id + " (already mapped to " + ids + ")"
                );
                case KEEP_EXISTING -> false;
                case REPLACE_EXISTING -> {
                    // Remove this key from whoever previously had it (index-level replace only).
                    for (UUID oldId : new ArrayList<>(ids)) {
                        LinkedHashSet<K> oldIdKeys = keysById.get(oldId);
                        if (oldIdKeys != null) {
                            oldIdKeys.remove(key);
                            if (oldIdKeys.isEmpty()) {
                                keysById.remove(oldId);
                            }
                        }
                    }
                    ids.clear();
                    ids.add(id);
                    yield true;
                }
            };
        }

        private void unlink(K key, UUID id) {
            LinkedHashSet<UUID> ids = idsByKey.get(key);
            if (ids == null) {
                return;
            }
            ids.remove(id);
            if (ids.isEmpty()) {
                idsByKey.remove(key);
            }
        }
    }

    // ---------------------------------------------------------------------
    // Full encode/decode (for persistence or full sync)
    // ---------------------------------------------------------------------

    /**
     * Full binary encode with the element StreamCodec.
     * Layout: int size, then that many T elements.
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

        boolean hasIndexes = !indexes.isEmpty();
        if (hasIndexes) {
            for (Index<?> index : indexes.values()) {
                index.onClear();
            }
        }

        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            T value = elementStreamCodec.decode(buf);
            UUID id = idGetter.apply(value);
            backing.put(id, value);

            if (hasIndexes) {
                for (Index<?> index : indexes.values()) {
                    index.onPut(id, value);
                }
            }
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
     *  - int changedCount, then changedCount * T
     *  - int removedCount, then removedCount * UUID
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
        boolean hasIndexes = !indexes.isEmpty();

        if (cleared) {
            backing.clear();
            if (hasIndexes) {
                for (Index<?> index : indexes.values()) {
                    index.onClear();
                }
            }
        }

        int changed = buf.readInt();
        for (int i = 0; i < changed; i++) {
            T value = elementStreamCodec.decode(buf);
            UUID id = idGetter.apply(value);
            backing.put(id, value);

            if (hasIndexes) {
                for (Index<?> index : indexes.values()) {
                    index.onPut(id, value);
                }
            }
        }

        int removedCount = buf.readInt();
        for (int i = 0; i < removedCount; i++) {
            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
            backing.remove(id);

            if (hasIndexes) {
                for (Index<?> index : indexes.values()) {
                    index.onRemove(id);
                }
            }
        }

        resetChangeTracking();
    }

    // ---------------------------------------------------------------------
    // Codec & StreamCodec factories
    // ---------------------------------------------------------------------
    // These are static so you can build the codecs where you register things.

    /**
     * Full Codec for persistence (JSON/NBT etc.). We just serialize as a list<T>.
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
        ) {
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
                        for (int i = 0; i < changed; i++) {
                            T obj = elementStreamCodec.decode(buf);
                            dirty.add(obj);
                        }

                        List<UUID> removed = new ArrayList<>();
                        int removedCount = buf.readInt();
                        for (int i = 0; i < removedCount; i++) {
                            UUID id = UUIDUtil.STREAM_CODEC.decode(buf);
                            removed.add(id);
                        }

                        return new DeltaPayload<>(
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
        boolean hasIndexes = !indexes.isEmpty();

        if (payload.cleared()) {
            backing.clear();
            if (hasIndexes) {
                for (Index<?> index : indexes.values()) {
                    index.onClear();
                }
            }
        }

        for (var obj : payload.dirty()) {
            UUID id = idGetter.apply(obj);
            backing.put(id, obj);

            if (hasIndexes) {
                for (Index<?> index : indexes.values()) {
                    index.onPut(id, obj);
                }
            }
        }

        for (var id : payload.removed()) {
            backing.remove(id);

            if (hasIndexes) {
                for (Index<?> index : indexes.values()) {
                    index.onRemove(id);
                }
            }
        }

        resetChangeTracking();
    }
}
