//package io.github.daxigua2333.mocai_clues.data.client.render_backing;
//
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.snapshot.ChunkSnapshot;
//import net.minecraft.client.Minecraft;
//import net.minecraft.world.level.ChunkPos;
//
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public final class CpuBuildScheduler {
//
//    private final ExecutorService pool;
//    private final ChunkCpuBuilder builder;
//
//    // latest revision per chunkKey (coalesce)
//    private final ConcurrentHashMap<Long, AtomicInteger> revisions = new ConcurrentHashMap<>();
//
//    // newest snapshot per chunkKey (game thread writes; worker reads from captured reference)
//    private final ConcurrentHashMap<Long, ChunkSnapshot> latestSnapshot = new ConcurrentHashMap<>();
//
//    // results for render thread to consume later
//    private final ConcurrentLinkedQueue<ChunkCpuBuildResult> ready = new ConcurrentLinkedQueue<>();
//
//    // simple in-flight limiter
//    private final Semaphore inflight;
//
//    public CpuBuildScheduler(ChunkCpuBuilder builder, int threads, int maxInflight) {
//        this.builder = builder;
//        this.pool = Executors.newFixedThreadPool(threads, r -> {
//            Thread t = new Thread(r, "my-mod-cpu-mesher");
//            t.setDaemon(true);
//            return t;
//        });
//        this.inflight = new Semaphore(maxInflight);
//    }
//
//    /** Game thread: publish a new snapshot and bump revision. */
//    public void onChunkDataChanged(ChunkPos cp, ChunkSnapshot snap) {
//        latestSnapshot.put(snap.chunkKey(), snap);
//        revisions.computeIfAbsent(snap.chunkKey(), k -> new AtomicInteger()).set(snap.revision());
//    }
//
//    /** Game thread: call every tick; schedules builds near player within a small budget. */
//    public void tickSchedule(int budgetChunksPerTick) {
//        int scheduled = 0;
//
//        for (Map.Entry<Long, ChunkSnapshot> e : latestSnapshot.entrySet()) {
//            if (scheduled >= budgetChunksPerTick) break;
//
//            long key = e.getKey();
//            ChunkSnapshot snap = e.getValue();
//
//            // If no permits, skip this tick.
//            if (!inflight.tryAcquire()) break;
//
//            scheduled++;
//
//            pool.execute(() -> {
//                try {
//                    // Revision check BEFORE heavy work (cheap skip).
//                    int latestRev = revisions.get(key).get();
//                    if (snap.revision() != latestRev) return;
//
//                    ChunkPos cp = new ChunkPos(key);
//                    ChunkCpuBuildResult res = builder.build(cp, snap);
//
//                    // Revision check AFTER build (drop stale results).
//                    latestRev = revisions.get(key).get();
//                    if (res.revision() != latestRev) {
//                        res.close(); // release CPU buffers
//                        return;
//                    }
//
//                    ready.add(res);
//                } catch (Throwable t) {
//                    t.printStackTrace();
//                } finally {
//                    inflight.release();
//                }
//            });
//        }
//    }
//
//    /** Render thread (later): poll finished CPU meshes and upload them. */
//    public ChunkCpuBuildResult pollReady() {
//        return ready.poll();
//    }
//
//    public void shutdown() {
//        pool.shutdownNow();
//        // Anything left in queue should be closed by caller on shutdown path.
//    }
//}
