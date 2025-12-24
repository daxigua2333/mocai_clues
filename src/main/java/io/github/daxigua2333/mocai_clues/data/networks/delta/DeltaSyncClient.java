//package io.github.daxigua2333.mocai_clues.data.networks.delta;
//
//
//
//import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
//import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
//import net.neoforged.neoforge.network.PacketDistributor;
//import net.neoforged.neoforge.network.handling.IPayloadContext;
//import org.dizitart.no2.Nitrite;
//import org.dizitart.no2.filters.ObjectFilters;
//import org.dizitart.no2.repository.ObjectRepository;
//
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.UUID;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.function.Supplier;
//
//public final class DeltaSyncClient {
//    private DeltaSyncClient() {}
//
//    private static volatile Supplier<Nitrite> CLIENT_DB = null;
//
//    private static volatile boolean ready = false;
//    private static final AtomicLong lastAppliedSeq = new AtomicLong(-1);
//
//    private static final Object BACKLOG_LOCK = new Object();
//    private static final Long2ObjectAVLTreeMap<DeltaOp> backlog = new Long2ObjectAVLTreeMap<>();
//
//    private static final AtomicLong pendingRequestFrom = new AtomicLong(Long.MIN_VALUE);
//
//    /** Call from client init AFTER you have replaced/opened the local DB snapshot. */
//    public static void bindClientDb(final Supplier<Nitrite> nitriteSupplier, final long snapshotBaseSeq, final boolean markReadyNow) {
//        CLIENT_DB = Objects.requireNonNull(nitriteSupplier);
//        lastAppliedSeq.set(snapshotBaseSeq);
//        ready = markReadyNow;
//        if (ready) flushBacklog();
//    }
//
//    /** Call when your client initialization is fully complete. */
//    public static void markReady() {
//        ready = true;
//        flushBacklog();
//    }
//
//    public static void handleDeltaBatch(final DeltaBatchPayload payload, final IPayloadContext ctx) {
//        ctx.enqueueWork(() -> {
//            final Supplier<Nitrite> dbSup = CLIENT_DB;
//            if (dbSup == null) return;
//
//            final long from = payload.fromSeq();
//            final List<DeltaOp> ops = payload.ops();
//
//            if (!ready) {
//                enqueueBacklog(from, ops);
//                return;
//            }
//
//            applyOrQueue(from, ops);
//            flushBacklog();
//        });
//    }
//
//    public static void handleResyncRequired(final ResyncRequiredPayload payload, final IPayloadContext ctx) {
//        ctx.enqueueWork(() -> {
//            // You can trigger your snapshot-replace flow here.
//            // This handler is intentionally minimal.
//            ready = false;
//            synchronized (BACKLOG_LOCK) {
//                backlog.clear();
//            }
//            pendingRequestFrom.set(Long.MIN_VALUE);
//        });
//    }
//
//    private static void enqueueBacklog(long fromSeq, List<DeltaOp> ops) {
//        synchronized (BACKLOG_LOCK) {
//            long seq = fromSeq;
//            for (DeltaOp op : ops) {
//                if (!backlog.containsKey(seq)) backlog.put(seq, op);
//                seq++;
//            }
//        }
//    }
//
//    private static void applyOrQueue(long fromSeq, List<DeltaOp> ops) {
//        long expected = lastAppliedSeq.get() + 1;
//
//        if (fromSeq > expected) {
//            enqueueBacklog(fromSeq, ops);
//            requestFrom(expected);
//            return;
//        }
//
//        long seq = fromSeq;
//        for (DeltaOp op : ops) {
//            final long cur = lastAppliedSeq.get();
//            if (seq <= cur) {
//                seq++;
//                continue;
//            }
//            if (seq != cur + 1) {
//                enqueueBacklog(seq, ops.subList((int) (seq - fromSeq), ops.size()));
//                requestFrom(cur + 1);
//                return;
//            }
//            applyOne(seq, op);
//            seq++;
//        }
//    }
//
//    private static void flushBacklog() {
//        if (!ready) return;
//
//        while (true) {
//            final long expected = lastAppliedSeq.get() + 1;
//            final DeltaOp op;
//
//            synchronized (BACKLOG_LOCK) {
//                op = backlog.remove(expected);
//            }
//
//            if (op == null) {
//                if (hasGap(expected)) requestFrom(expected);
//                return;
//            }
//
//            applyOne(expected, op);
//        }
//    }
//
//    private static boolean hasGap(long expected) {
//        synchronized (BACKLOG_LOCK) {
//            if (backlog.isEmpty()) return false;
//            final long first = backlog.firstLongKey();
//            return first > expected;
//        }
//    }
//
//    private static void applyOne(long seq, DeltaOp op) {
//        final Nitrite db = CLIENT_DB.get();
//        final ObjectRepository<MyObject> repo = db.getRepository(MyObject.class);
//
//        switch (op) {
//            case DeltaOp.Upsert u -> repo.update(u.value(), true);
//            case DeltaOp.Delete d -> repo.remove(ObjectFilters.eq("id", d.id()));
//        }
//
//        lastAppliedSeq.set(seq);
//        if (pendingRequestFrom.get() == (seq + 1)) pendingRequestFrom.set(Long.MIN_VALUE);
//    }
//
//    private static void requestFrom(long fromSeq) {
//        final long prev = pendingRequestFrom.getAndUpdate(old -> {
//            if (old == Long.MIN_VALUE) return fromSeq;
//            return Math.min(old, fromSeq);
//        });
//
//        if (prev != Long.MIN_VALUE && prev <= fromSeq) return;
//
//        sendToServerCompat(new DeltaRequestPayload(fromSeq));
//    }
//
//    private static void sendToServerCompat(Object payload) {
//        // 1.21 - 1.21.1 uses PacketDistributor.sendToServer(...)
//        // Newer NeoForge uses ClientPacketDistributor.sendToServer(...)
//        try {
//            final Class<?> pd = Class.forName("net.neoforged.neoforge.network.PacketDistributor");
//            final Method m = pd.getMethod("sendToServer", Class.forName("net.minecraft.network.protocol.common.custom.CustomPacketPayload"));
//            m.invoke(null, payload);
//            return;
//        } catch (Throwable ignored) {}
//
//        try {
//            final Class<?> cpd = Class.forName("net.neoforged.neoforge.network.ClientPacketDistributor");
//            final Method m = cpd.getMethod("sendToServer", Class.forName("net.minecraft.network.protocol.common.custom.CustomPacketPayload"));
//            m.invoke(null, payload);
//        } catch (Throwable ignored) {}
//    }
//}