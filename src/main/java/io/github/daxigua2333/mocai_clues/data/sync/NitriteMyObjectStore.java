package io.github.daxigua2333.mocai_clues.data.sync;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.sync.misc.DocumentCodecIO;
import io.github.daxigua2333.mocai_clues.data.sync.misc.MyObjectKeyProvider;
import io.github.daxigua2333.mocai_clues.data.sync.misc.MyObjectOpRecord;
import io.github.daxigua2333.mocai_clues.data.sync.misc.MyObjectOpType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class NitriteMyObjectStore {

    public enum Mode { SERVER, CLIENT }

    private static final String C_DATA = "myobject_data";
    private static final String C_OPLOG = "myobject_oplog";
    private static final String C_META = "myobject_meta";

    /** used in meta(config) collection like doc1: {k: meta, v:..}, {k: config, v:}
     * also in data collection, because UUID can be sorted in time order*/
    private static final String F_KEY = "k";
    private static final String F_VAL = "v";

    private static final String F_SEQ = "seq";
    private static final String F_OP = "op";
    private static final String F_TS = "ts";
    private static final String F_LAST_APPLIED = "last_applied";
    private static final String F_NEXT_SEQ = "next_seq";

    private static final String META_KEY = "meta";
    private final Nitrite db;
    private final Mode mode;
    private final MyObjectKeyProvider keyProvider;

    private final NitriteCollection data;
    private final NitriteCollection oplog;
    private final NitriteCollection meta;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final List<OpListener> listeners = new CopyOnWriteArrayList<>();

    public interface OpListener {
        void onOpAppended(MyObjectOpRecord op);
    }

    public NitriteMyObjectStore(Nitrite db, Mode mode, MyObjectKeyProvider keyProvider) {
        this.db = Objects.requireNonNull(db);
        this.mode = Objects.requireNonNull(mode);
        this.keyProvider = Objects.requireNonNull(keyProvider);

        this.data = db.getCollection(C_DATA);
        this.oplog = db.getCollection(C_OPLOG);
        this.meta = db.getCollection(C_META);

        initCollections();
    }

    private void initCollections() {
        lock.writeLock().lock();
        try {
            try {
                data.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), F_KEY);
            } catch (Exception ignored) {}

            try {
                oplog.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), F_SEQ);
            } catch (Exception ignored) {}

            try {
                meta.createIndex(IndexOptions.indexOptions(IndexType.UNIQUE), F_KEY);
            } catch (Exception ignored) {}

            ensureMetaDefaults();

        } finally {
            lock.writeLock().unlock();
        }
    }


    private Filter metaFilter() {
        return FluentFilter.where(F_KEY).eq(META_KEY);
    }

    private Document metaDocOrThrow() {
        Document m = meta.find(metaFilter()).firstOrNull();
        if (m == null) {
            throw new IllegalStateException("Meta document is missing; database not initialized");
        }
        return m;
    }

    private long maxOplogSeqOrZero() {
        Document last = oplog.find(FindOptions.orderBy(F_SEQ, SortOrder.Descending).limit(1)).firstOrNull();
        if (last == null) return 0L;
        Long v = last.get(F_SEQ, Long.class);
        return v == null ? 0L : v;
    }

    private void ensureMetaDefaults() {
        Document m = meta.find(metaFilter()).firstOrNull();

        // Backward compatibility: older versions stored a singleton meta document identified only by _id.
        if (m == null) {
            Document legacy = meta.find(Filter.ALL).firstOrNull();
            if (legacy != null) {
                legacy.put(F_KEY, META_KEY);
                meta.update(Filter.byId(legacy.getId()), legacy);
                m = legacy;
            }
        }

        if (m == null) {
            m = Document.createDocument(F_KEY, META_KEY);
            if (mode == Mode.SERVER) {
                long maxSeq = maxOplogSeqOrZero();
                m.put(F_NEXT_SEQ, maxSeq + 1L);
            } else {
                m.put(F_LAST_APPLIED, 0L);
            }
            meta.insert(m);
            return;
        }

        if (mode == Mode.SERVER) {
            long maxSeq = maxOplogSeqOrZero();
            Long nextSeq = m.get(F_NEXT_SEQ, Long.class);
            long desiredNextSeq = Math.max(nextSeq == null ? 0L : nextSeq, maxSeq + 1L);
            if (nextSeq == null || desiredNextSeq != nextSeq) {
                m.put(F_NEXT_SEQ, desiredNextSeq);
                meta.update(metaFilter(), m);
            }
        } else {
            if (!m.containsKey(F_LAST_APPLIED)) {
                m.put(F_LAST_APPLIED, 0L);
                meta.update(metaFilter(), m);
            }
        }
    }

    public void addListener(OpListener listener) {
        listeners.add(listener);
    }

    public void removeListener(OpListener listener) {
        listeners.remove(listener);
    }

    /**
     * Creates an index on one or more {@link ClueObject} fields.
     *
     * <p>Since {@link ClueObject} is stored under the nested field {@code "v"}, indexing a field named
     * {@code "foo"} is done by creating an index on {@code "v.foo"}. Nested fields can be specified using
     * Nitrite's field separator (default {@code '.'}).
     */
    public void createClueFieldIndex(IndexOptions options, String... clueFieldPaths) {
        Objects.requireNonNull(options);
        Objects.requireNonNull(clueFieldPaths);
        if (clueFieldPaths.length == 0) return;

        String[] nitriteFields = new String[clueFieldPaths.length];
        for (int i = 0; i < clueFieldPaths.length; i++) {
            String p = Objects.requireNonNull(clueFieldPaths[i], "clueFieldPaths[" + i + "]");
            nitriteFields[i] = F_VAL + "." + p;
        }

        lock.writeLock().lock();
        try {
            data.createIndex(options, nitriteFields);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Convenience overload: creates a unique index on the specified {@link ClueObject} field path(s).
    public void createClueFieldIndex(String... clueFieldPaths) {
        createClueFieldIndex(IndexOptions.indexOptions(IndexType.UNIQUE), clueFieldPaths);
    }


    /**
     * these 2 serve the start and end Long of oplog sequence
     * */
    public long serverCurrentSeq() {
        requireMode(Mode.SERVER);
        lock.readLock().lock();
        try {
            Document m = metaDocOrThrow();
            long next = m.get(F_NEXT_SEQ, Long.class);
            return Math.max(0L, next - 1L);
        } finally {
            lock.readLock().unlock();
        }
    }
    public long serverMinAvailableSeqExclusive() {
        requireMode(Mode.SERVER);
        lock.readLock().lock();
        try {
            Document first = oplog.find(FindOptions.orderBy(F_SEQ, SortOrder.Ascending).limit(1)).firstOrNull();
            if (first == null) return serverCurrentSeq(); // empty => only current seq is meaningful
            long minSeq = first.get(F_SEQ, Long.class);
            return Math.max(0L, minSeq - 1L);
        } finally {
            lock.readLock().unlock();
        }
    }

    /** client seq */
    public long clientLastAppliedSeq() {
        requireMode(Mode.CLIENT);
        lock.readLock().lock();
        try {
            Document m = metaDocOrThrow();
            return m.get(F_LAST_APPLIED, Long.class);
        } finally {
            lock.readLock().unlock();
        }
    }
    public void clientSetLastAppliedSeq(long seq) {
        requireMode(Mode.CLIENT);
        lock.writeLock().lock();
        try {
            Document m = metaDocOrThrow();
            m.put(F_LAST_APPLIED, seq);
            meta.update(metaFilter(), m);
        } finally {
            lock.writeLock().unlock();
        }
    }


    /** 2 server side write behaviors*/
    public void serverUpsert(ClueObject obj) {
        requireMode(Mode.SERVER);
        String key = keyProvider.keyOf(obj);
        Document valueDoc = DocumentCodecIO.encodeToDocument(ClueObject.CODEC, obj);

        lock.writeLock().lock();
        try {
            long seq = nextSeqLocked();
            upsertValueLocked(key, valueDoc);
            appendOpLocked(MyObjectOpRecord.upsert(seq, key, obj), valueDoc);
        } finally {
            lock.writeLock().unlock();
        }
    }
    public void serverDelete(String key) {
        requireMode(Mode.SERVER);

        lock.writeLock().lock();
        try {
            long seq = nextSeqLocked();
            deleteLocked(key);
            appendOpLocked(MyObjectOpRecord.delete(seq, key), null);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves a single {@link ClueObject} by its logical key from the data collection.
     *
     * <p>This is a read-only operation and is valid in both {@link Mode#SERVER} and
     * {@link Mode#CLIENT} modes.
     */
    public Optional<ClueObject> retrieve(String key) {
        Objects.requireNonNull(key, "key");
        lock.readLock().lock();
        try {
            Document doc = data.find(FluentFilter.where(F_KEY).eq(key)).firstOrNull();
            if (doc == null) return Optional.empty();
            return Optional.of(decodeValueOrThrow(doc.get(F_VAL)));
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Helper to reference a {@link ClueObject} field path inside the stored Nitrite document.
     *
     * <p>All {@link ClueObject} data is stored under the nested field {@code "v"}. Therefore,
     * to filter/sort on a {@link ClueObject} field named {@code "foo"}, you should use
     * {@code NitriteMyObjectStore.clueField("foo")} which returns {@code "v.foo"}.
     */
    public static String clueField(String clueFieldPath) {
        Objects.requireNonNull(clueFieldPath, "clueFieldPath");
        return F_VAL + "." + clueFieldPath;
    }

    /**
     * Retrieves {@link ClueObject}(s) by applying a Nitrite {@link Filter} and optional {@link FindOptions}.
     *
     * <p><strong>Important:</strong> since the value is stored under the nested field {@code "v"},
     * the provided {@code filter} (and any {@code FindOptions#orderBy} field) should reference
     * nested fields such as {@code "v.someField"}. Use {@link #clueField(String)} to build those paths.
     *
     * <p>Examples:
     * <pre>
     * // equality
     * store.retrieve(FluentFilter.where(NitriteMyObjectStore.clueField("type")).eq("X"));
     *
     * // paging + sorting
     * var opts = FindOptions.orderBy(NitriteMyObjectStore.clueField("updatedAt"), SortOrder.Descending)
     *     .skip(0)
     *     .limit(50);
     * store.retrieve(FluentFilter.where(NitriteMyObjectStore.clueField("status")).eq("OPEN"), opts);
     * </pre>
     */
    public List<ClueObject> retrieve(Filter filter, FindOptions options) {
        Objects.requireNonNull(filter, "filter");
        lock.readLock().lock();
        try {
            List<ClueObject> result = new ArrayList<>();
            var cursor = (options == null) ? data.find(filter) : data.find(filter, options);
            for (Document doc : cursor) {
                result.add(decodeValueOrThrow(doc.get(F_VAL)));
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Convenience overload for {@link #retrieve(Filter, FindOptions)} with no {@link FindOptions}. */
    public List<ClueObject> retrieve(Filter filter) {
        return retrieve(filter, null);
    }

    /**
     * Retrieves the first matching {@link ClueObject} for a filter/options pair.
     *
     * <p>If you want a stable "first" when multiple records match, pass an {@link FindOptions}
     * with {@code orderBy(...)} and {@code limit(1)}.
     */
    public Optional<ClueObject> retrieveFirst(Filter filter, FindOptions options) {
        Objects.requireNonNull(filter, "filter");
        lock.readLock().lock();
        try {
            Document doc = (options == null) ? data.find(filter).firstOrNull() : data.find(filter, options).firstOrNull();
            if (doc == null) return Optional.empty();
            return Optional.of(decodeValueOrThrow(doc.get(F_VAL)));
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Convenience overload for {@link #retrieveFirst(Filter, FindOptions)} with no {@link FindOptions}. */
    public Optional<ClueObject> retrieveFirst(Filter filter) {
        return retrieveFirst(filter, null);
    }

//    public void findData() {
//        for (var doc : data.find()) {
//            MoCaiClues.LOGGER.debug("{}\n\n", doc);
//        }
//    }

    /** client behaviors */
    public void clientApplySnapshotClearAndBegin(long baseSeq) {
        requireMode(Mode.CLIENT);
        lock.writeLock().lock();
        try {
            data.remove(Filter.ALL);
            oplog.remove(Filter.ALL);
            clientSetLastAppliedSeq(baseSeq);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clientApplySnapshotUpsert(ClueObject obj, String keyOverrideOrNull) {
        requireMode(Mode.CLIENT);
        String key = keyOverrideOrNull != null ? keyOverrideOrNull : keyProvider.keyOf(obj);
        Document valueDoc = DocumentCodecIO.encodeToDocument(ClueObject.CODEC, obj);

        lock.writeLock().lock();
        try {
            upsertValueLocked(key, valueDoc);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void clientApplyRemoteOp(MyObjectOpRecord op) {
        requireMode(Mode.CLIENT);

        lock.writeLock().lock();
        try {
            long last = clientLastAppliedSeq();
            if (op.seq() <= last) return;
            if (op.seq() != last + 1L) {
                throw new IllegalStateException("Gap detected: last=" + last + " incoming=" + op.seq());
            }

            if (op.type() == MyObjectOpType.UPSERT) {
                ClueObject val = op.value().orElseThrow(() -> new IllegalStateException("UPSERT missing value"));
                Document valueDoc = DocumentCodecIO.encodeToDocument(ClueObject.CODEC, val);
                upsertValueLocked(op.key(), valueDoc);
                appendClientOplogLocked(op, valueDoc);
            } else {
                deleteLocked(op.key());
                appendClientOplogLocked(op, null);
            }

            clientSetLastAppliedSeq(op.seq());
        } finally {
            lock.writeLock().unlock();
        }
    }


    /** get the list of client behind seq from server */
    public List<MyObjectOpRecord> serverGetOpsAfter(long afterSeqExclusive, int limit) {
        requireMode(Mode.SERVER);
        lock.readLock().lock();
        try {
            List<MyObjectOpRecord> out = new ArrayList<>();
            var cursor = oplog.find(FluentFilter.where(F_SEQ).gt(afterSeqExclusive),
                    FindOptions.orderBy(F_SEQ, SortOrder.Ascending).limit(limit));
            for (Document d : cursor) {
                out.add(decodeOpDoc(d));
            }
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }

    /** get snapshot */
    public List<ClueObject> serverSnapshotAllObjectsSorted() {
        requireMode(Mode.SERVER);
        lock.readLock().lock();
        try {
            List<ClueObject> out = new ArrayList<>();
            var cursor = data.find(FindOptions.orderBy(F_KEY, SortOrder.Ascending));
            for (Document d : cursor) {
                out.add(decodeValueOrThrow(d.get(F_VAL)));
            }
            return out;
        } finally {
            lock.readLock().unlock();
        }
    }
    public int serverSnapshotCount() {
        requireMode(Mode.SERVER);
        lock.readLock().lock();
        try {
            return (int) data.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    /** Computes a SHA-256 digest over the sorted (key, valueBytes) pairs.
     * Used to check if client and server are the same */
//    public String serverStateDigestHex() {
//        requireMode(Mode.SERVER);
//        lock.readLock().lock();
//        try {
//            MessageDigest md = MessageDigest.getInstance("SHA-256");
//            var cursor = data.find(FindOptions.orderBy(F_KEY, SortOrder.Ascending));
//            for (Document d : cursor) {
//                String k = d.get(F_KEY, String.class);
//                md.update(k.getBytes(java.nio.charset.StandardCharsets.UTF_8));
//                md.update((byte) 0);
//                updateDigest(md, d.get(F_VAL));
//                md.update((byte) 0);
//            }
//            return HexFormat.of().formatHex(md.digest());
//        } catch (Exception e) {
//            throw new IllegalStateException("Digest failed", e);
//        } finally {
//            lock.readLock().unlock();
//        }
//    }

    /** delete unneeded oplog, to limit its length */
    public void serverPruneOplogIfNeeded() {
        requireMode(Mode.SERVER);
        lock.writeLock().lock();
        try {
            long count = oplog.size();
            if (count <= SyncConstants.OPLOG_MAX_ENTRIES) return;

            long target = Math.max(SyncConstants.OPLOG_MIN_ENTRIES, SyncConstants.OPLOG_MAX_ENTRIES);
            long toRemove = Math.max(0L, count - target);
            if (toRemove <= 0) return;

            var cursor = oplog.find(FindOptions.orderBy(F_SEQ, SortOrder.Ascending).limit((int) Math.min(Integer.MAX_VALUE, toRemove)));
            List<Long> seqs = new ArrayList<>();
            for (Document d : cursor) {
                seqs.add(d.get(F_SEQ, Long.class));
            }
            for (Long seq : seqs) {
                oplog.remove(FluentFilter.where(F_SEQ).eq(seq));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * many internal helpers
     * */

    // server increments seq
    private long nextSeqLocked() {
        Document m = metaDocOrThrow();
        long next = m.get(F_NEXT_SEQ, Long.class);
        long seq = next;
        m.put(F_NEXT_SEQ, next + 1L);
        meta.update(metaFilter(), m);
        return seq;
    }

    /** actual part that upsert/delete DATA coll */
//    private void upsertBytesLocked(String key, byte[] bytes) {
//        throw new UnsupportedOperationException("Binary value storage is no longer supported; use upsertValueLocked");
//    }
    private void upsertValueLocked(String key, Document valueDoc) {
        Document doc = Document.createDocument(F_KEY, key).put(F_VAL, valueDoc);
        data.update(FluentFilter.where(F_KEY).eq(key), doc, UpdateOptions.updateOptions(true));
    }
    private void deleteLocked(String key) {
        data.remove(FluentFilter.where(F_KEY).eq(key));
    }

    /** server/client append new oplog */
    private void appendOpLocked(MyObjectOpRecord op, Document encodedValueOrNull) {
        Document doc = Document.createDocument(F_SEQ, op.seq())
                .put(F_OP, op.opType())
                .put(F_KEY, op.key())
                .put(F_TS, System.currentTimeMillis());
        if (encodedValueOrNull != null) doc.put(F_VAL, encodedValueOrNull);
        oplog.insert(doc);

        for (OpListener l : listeners) {
            try {
                l.onOpAppended(op);
            } catch (Exception ignored) {}
        }
    }
    private void appendClientOplogLocked(MyObjectOpRecord op, Document encodedValueOrNull) {
        Document doc = Document.createDocument(F_SEQ, op.seq())
                .put(F_OP, op.opType())
                .put(F_KEY, op.key())
                .put(F_TS, System.currentTimeMillis());
        if (encodedValueOrNull != null) doc.put(F_VAL, encodedValueOrNull);
        try {
            oplog.insert(doc);
        } catch (Exception e) {
            oplog.update(FluentFilter.where(F_SEQ).eq(op.seq()), doc, UpdateOptions.updateOptions(true));
        }
    }

    // convert OpDoc to OpRecord
    private MyObjectOpRecord decodeOpDoc(Document d) {
        long seq = d.get(F_SEQ, Long.class);
        byte op = d.get(F_OP, Byte.class);
        String key = d.get(F_KEY, String.class);

        if (MyObjectOpType.fromId(op) == MyObjectOpType.UPSERT) {
            ClueObject obj = decodeValueOrThrow(d.get(F_VAL));
            return MyObjectOpRecord.upsert(seq, key, obj);
        }
        return MyObjectOpRecord.delete(seq, key);
    }

    /**
     * Decodes a stored value from either the new document format or the legacy compressed byte[] format.
     */
    private static ClueObject decodeValueOrThrow(Object raw) {
        if (raw == null) {
            throw new IllegalStateException("Missing value field '" + F_VAL + "'");
        }
        if (raw instanceof Document doc) {
            return DocumentCodecIO.decodeFromDocument(ClueObject.CODEC, doc);
        }
        if (raw instanceof Map<?, ?> map) {
            // Defensive: depending on store adapter, nested documents may be materialized as plain maps.
            Document d = Document.createDocument();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() instanceof String k) d.put(k, e.getValue());
            }
            return DocumentCodecIO.decodeFromDocument(ClueObject.CODEC, d);
        }
//        if (raw instanceof byte[] bytes) {
//            return NbtCodecIO.decodeFromBytes(ClueObject.CODEC, bytes);
//        }
        throw new IllegalStateException("Unsupported stored value type: " + raw.getClass());
    }

//    // Deterministic digest for nested documents/lists/primitives.
//    private static void updateDigest(MessageDigest md, Object v) {
//        if (v == null) {
//            md.update((byte) 0);
//            return;
//        }
//        if (v instanceof byte[] bytes) {
//            md.update((byte) 1);
//            putInt(md, bytes.length);
//            md.update(bytes);
//            return;
//        }
//        if (v instanceof Document d) {
//            md.update((byte) 2);
//            updateDigestForMap(md, d);
//            return;
//        }
//        if (v instanceof Map<?, ?> map) {
//            md.update((byte) 3);
//            updateDigestForMap(md, map);
//            return;
//        }
//        if (v instanceof List<?> list) {
//            md.update((byte) 4);
//            putInt(md, list.size());
//            for (Object o : list) {
//                updateDigest(md, o);
//            }
//            return;
//        }
//        if (v instanceof String s) {
//            md.update((byte) 5);
//            byte[] b = s.getBytes(java.nio.charset.StandardCharsets.UTF_8);
//            putInt(md, b.length);
//            md.update(b);
//            return;
//        }
//        if (v instanceof Boolean b) {
//            md.update((byte) 6);
//            md.update((byte) (b ? 1 : 0));
//            return;
//        }
//        if (v instanceof Integer i) {
//            md.update((byte) 7);
//            putInt(md, i);
//            return;
//        }
//        if (v instanceof Long l) {
//            md.update((byte) 8);
//            putLong(md, l);
//            return;
//        }
//        if (v instanceof Short s) {
//            md.update((byte) 9);
//            putInt(md, s);
//            return;
//        }
//        if (v instanceof Byte b) {
//            md.update((byte) 10);
//            md.update(b);
//            return;
//        }
//        if (v instanceof Float f) {
//            md.update((byte) 11);
//            putInt(md, Float.floatToIntBits(f));
//            return;
//        }
//        if (v instanceof Double d) {
//            md.update((byte) 12);
//            putLong(md, Double.doubleToLongBits(d));
//            return;
//        }
//        if (v instanceof int[] arr) {
//            md.update((byte) 13);
//            putInt(md, arr.length);
//            for (int x : arr) putInt(md, x);
//            return;
//        }
//        if (v instanceof long[] arr) {
//            md.update((byte) 14);
//            putInt(md, arr.length);
//            for (long x : arr) putLong(md, x);
//            return;
//        }
//        // Fallback
//        md.update((byte) 127);
//        byte[] b = String.valueOf(v).getBytes(java.nio.charset.StandardCharsets.UTF_8);
//        putInt(md, b.length);
//        md.update(b);
//    }
//
//    private static void updateDigestForMap(MessageDigest md, Map<?, ?> map) {
//        List<String> keys = new ArrayList<>();
//        for (Object k : map.keySet()) {
//            if (k instanceof String s) keys.add(s);
//        }
//        Collections.sort(keys);
//        putInt(md, keys.size());
//        for (String k : keys) {
//            byte[] kb = k.getBytes(java.nio.charset.StandardCharsets.UTF_8);
//            putInt(md, kb.length);
//            md.update(kb);
//            md.update((byte) 0);
//            updateDigest(md, map.get(k));
//            md.update((byte) 0);
//        }
//    }
//
//    private static void putInt(MessageDigest md, int v) {
//        md.update((byte) (v >>> 24));
//        md.update((byte) (v >>> 16));
//        md.update((byte) (v >>> 8));
//        md.update((byte) (v));
//    }
//
//    private static void putLong(MessageDigest md, long v) {
//        md.update((byte) (v >>> 56));
//        md.update((byte) (v >>> 48));
//        md.update((byte) (v >>> 40));
//        md.update((byte) (v >>> 32));
//        md.update((byte) (v >>> 24));
//        md.update((byte) (v >>> 16));
//        md.update((byte) (v >>> 8));
//        md.update((byte) (v));
//    }

    private void requireMode(Mode expected) {
        if (mode != expected) {
            throw new IllegalStateException("Store mode mismatch. Expected " + expected + " but was " + mode);
        }
    }
}