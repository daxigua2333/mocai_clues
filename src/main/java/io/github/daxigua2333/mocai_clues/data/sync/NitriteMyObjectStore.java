package io.github.daxigua2333.mocai_clues.data.sync;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.collection.*;
import org.dizitart.no2.common.SortOrder;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.filters.FluentFilter;
import org.dizitart.no2.index.IndexOptions;
import org.dizitart.no2.index.IndexType;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class NitriteMyObjectStore {

    public enum Mode { SERVER, CLIENT }

    private static final String C_DATA = "myobject_data";
    private static final String C_OPLOG = "myobject_oplog";
    private static final String C_META = "myobject_meta";

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

    public void serverUpsert(ClueObject obj) {
        requireMode(Mode.SERVER);
        String key = keyProvider.keyOf(obj);
        byte[] bytes = NbtCodecIO.encodeToBytes(ClueObject.CODEC, obj);

        lock.writeLock().lock();
        try {
            long seq = nextSeqLocked();
            upsertBytesLocked(key, bytes);
            appendOpLocked(MyObjectOpRecord.upsert(seq, key, obj), bytes);
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

    public List<ClueObject> find() {
        List<ClueObject> result = new ArrayList<>();
        var cursor = data.find();
        for (Document doc : cursor) {
            byte[] bytes = (byte[]) doc.get(F_VAL);
            result.add(NbtCodecIO.decodeFromBytes(ClueObject.CODEC, bytes));
        }
        return result;
    }

    public void findData() {
        for (var doc : oplog.find()) {
            MoCaiClues.LOGGER.debug("{}\n\n", doc);
        }
    }

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
        byte[] bytes = NbtCodecIO.encodeToBytes(ClueObject.CODEC, obj);

        lock.writeLock().lock();
        try {
            upsertBytesLocked(key, bytes);
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
                byte[] bytes = NbtCodecIO.encodeToBytes(ClueObject.CODEC, val);
                upsertBytesLocked(op.key(), bytes);
                appendClientOplogLocked(op, bytes);
            } else {
                deleteLocked(op.key());
                appendClientOplogLocked(op, null);
            }

            clientSetLastAppliedSeq(op.seq());
        } finally {
            lock.writeLock().unlock();
        }
    }

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

    public List<ClueObject> serverSnapshotAllObjectsSorted() {
        requireMode(Mode.SERVER);
        lock.readLock().lock();
        try {
            List<ClueObject> out = new ArrayList<>();
            var cursor = data.find(FindOptions.orderBy(F_KEY, SortOrder.Ascending));
            for (Document d : cursor) {
                byte[] bytes = d.get(F_VAL, byte[].class);
                out.add(NbtCodecIO.decodeFromBytes(ClueObject.CODEC, bytes));
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

    public String serverStateDigestHex() {
        requireMode(Mode.SERVER);
        lock.readLock().lock();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            var cursor = data.find(FindOptions.orderBy(F_KEY, SortOrder.Ascending));
            for (Document d : cursor) {
                String k = d.get(F_KEY, String.class);
                byte[] v = d.get(F_VAL, byte[].class);
                md.update(k.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                md.update((byte) 0);
                md.update(v);
                md.update((byte) 0);
            }
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new IllegalStateException("Digest failed", e);
        } finally {
            lock.readLock().unlock();
        }
    }

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

    private long nextSeqLocked() {
        Document m = metaDocOrThrow();
        long next = m.get(F_NEXT_SEQ, Long.class);
        long seq = next;
        m.put(F_NEXT_SEQ, next + 1L);
        meta.update(metaFilter(), m);
        return seq;
    }

    private void upsertBytesLocked(String key, byte[] bytes) {
        Document doc = Document.createDocument(F_KEY, key).put(F_VAL, bytes);
        data.update(FluentFilter.where(F_KEY).eq(key), doc, UpdateOptions.updateOptions(true));
    }

    private void deleteLocked(String key) {
        data.remove(FluentFilter.where(F_KEY).eq(key));
    }

    private void appendOpLocked(MyObjectOpRecord op, byte[] encodedValueOrNull) {
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

    private void appendClientOplogLocked(MyObjectOpRecord op, byte[] encodedValueOrNull) {
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

    private MyObjectOpRecord decodeOpDoc(Document d) {
        long seq = d.get(F_SEQ, Long.class);
        byte op = d.get(F_OP, Byte.class);
        String key = d.get(F_KEY, String.class);

        if (MyObjectOpType.fromId(op) == MyObjectOpType.UPSERT) {
            byte[] bytes = d.get(F_VAL, byte[].class);
            ClueObject obj = NbtCodecIO.decodeFromBytes(ClueObject.CODEC, bytes);
            return MyObjectOpRecord.upsert(seq, key, obj);
        }
        return MyObjectOpRecord.delete(seq, key);
    }

    private void requireMode(Mode expected) {
        if (mode != expected) {
            throw new IllegalStateException("Store mode mismatch. Expected " + expected + " but was " + mode);
        }
    }
}