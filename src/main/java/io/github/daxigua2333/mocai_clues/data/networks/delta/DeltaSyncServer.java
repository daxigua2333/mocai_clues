//package io.github.daxigua2333.mocai_clues.data.networks.delta;
//
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
//import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
//import net.minecraft.server.level.ServerPlayer;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.fml.common.Mod;
//import net.neoforged.neoforge.event.tick.ServerTickEvent;
//import net.neoforged.neoforge.network.PacketDistributor;
//import net.neoforged.neoforge.network.handling.IPayloadContext;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.atomic.AtomicLong;
//
//@EventBusSubscriber(modid = MoCaiClues.MODID)
//public final class DeltaSyncServer {
//    private DeltaSyncServer() {}
//
//    private static final int MAX_LOG_OPS = 100_000;
//    private static final int MAX_PACKET_OPS = 256;
//
//    private static final AtomicLong NEXT_SEQ = new AtomicLong(0);
//    private static final Long2ObjectAVLTreeMap<DeltaOp> LOG = new Long2ObjectAVLTreeMap<>();
//    private static final Object LOG_LOCK = new Object();
//
//    private static final ConcurrentLinkedQueue<SequencedOp> PENDING = new ConcurrentLinkedQueue<>();
//
//    private record SequencedOp(long seq, DeltaOp op) {}
//
//    public static long currentSeq() {
//        return NEXT_SEQ.get();
//    }
//
//    public static long recordUpsert(ClueObject obj) {
//        return record(new DeltaOp.Upsert(obj));
//    }
//
//    public static long recordDelete(java.util.UUID id) {
//        return record(new DeltaOp.Delete(id));
//    }
//
//    private static long record(DeltaOp op) {
//        final long seq = NEXT_SEQ.getAndIncrement();
//
//        synchronized (LOG_LOCK) {
//            LOG.put(seq, op);
//            while (LOG.size() > MAX_LOG_OPS) {
//                final long firstKey = LOG.firstLongKey();
//                LOG.remove(firstKey);
//            }
//        }
//
//        PENDING.add(new SequencedOp(seq, op));
//        return seq;
//    }
//
//    @SubscribeEvent
//    public static void onServerTick(final ServerTickEvent.Post event) {
//        if (PENDING.isEmpty()) return;
//
//        final ArrayList<SequencedOp> drained = new ArrayList<>();
//        for (SequencedOp op; (op = PENDING.poll()) != null; ) drained.add(op);
//        if (drained.isEmpty()) return;
//
//        drained.sort(Comparator.comparingLong(SequencedOp::seq));
//
//        long runStart = -1;
//        long expected = -1;
//        final ArrayList<DeltaOp> runOps = new ArrayList<>(Math.min(drained.size(), MAX_PACKET_OPS));
//
//        for (final SequencedOp e : drained) {
//            if (runStart < 0) {
//                runStart = e.seq;
//                expected = e.seq;
//            }
//            if (e.seq != expected || runOps.size() >= MAX_PACKET_OPS) {
//                PacketDistributor.sendToAllPlayers(new DeltaBatchPayload(runStart, List.copyOf(runOps)));
//                runOps.clear();
//                runStart = e.seq;
//                expected = e.seq;
//            }
//            runOps.add(e.op);
//            expected++;
//        }
//
//        if (!runOps.isEmpty()) {
//            PacketDistributor.sendToAllPlayers(new DeltaBatchPayload(runStart, List.copyOf(runOps)));
//        }
//    }
//
//    public static void handleDeltaRequest(final DeltaRequestPayload req, final IPayloadContext ctx) {
//        ctx.enqueueWork(() -> {
//            final ServerPlayer player = (ServerPlayer) ctx.player();
//            long from = req.fromSeq();
//
//            while (true) {
//                final Range range = snapshotRange(from, MAX_PACKET_OPS);
//                if (range.ops.isEmpty()) return;
//
//                PacketDistributor.sendToPlayer(player, new DeltaBatchPayload(range.fromSeq, range.ops));
//                from = range.fromSeq + range.ops.size();
//
//                if (range.completeToEnd) return;
//            }
//        });
//    }
//
//    private record Range(long fromSeq, List<DeltaOp> ops, boolean completeToEnd) {}
//
//    private static Range snapshotRange(long fromSeq, int limit) {
//        final ArrayList<DeltaOp> out = new ArrayList<>(limit);
//
//        synchronized (LOG_LOCK) {
//            if (LOG.isEmpty()) {
//                return new Range(fromSeq, List.of(), true);
//            }
//
//            final long first = LOG.firstLongKey();
//            final long last = LOG.lastLongKey();
//
//            if (fromSeq < first) {
//                return new Range(fromSeq, List.of(), true);
//            }
//            if (fromSeq > last) {
//                return new Range(fromSeq, List.of(), true);
//            }
//
//            long expected = fromSeq;
//
//            for (final Long2ObjectMap.Entry<DeltaOp> ent : LOG.tailMap(fromSeq).long2ObjectEntrySet()) {
//                final long k = ent.getLongKey();
//                if (k != expected) break;
//                out.add(ent.getValue());
//                expected++;
//                if (out.size() >= limit) break;
//            }
//
//            final boolean completeToEnd = (expected > last);
//            return new Range(fromSeq, List.copyOf(out), completeToEnd);
//        }
//    }
//}