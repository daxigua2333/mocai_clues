//package io.github.daxigua2333.mocai_clues.data.client.render_backing.snapshot;
//
//import io.github.daxigua2333.mocai_clues.component.ClueObject;
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.level.ChunkPos;
//import java.util.Map;
//import java.util.UUID;
//
//public final class SnapshotFactory {
//
//    public static ChunkSnapshot buildSnapshot(
//            ChunkPos cp,
//            int revision,
//            Map<UUID, ClueObject> chunkMap,
//            Iterable<ClueObject> extraFromSavedDataInThisChunk // optional
//    ) {
//        // Estimate size to minimize realloc
//        int n = chunkMap.size();
//        // If you can count extraFromSavedData cheaply, add it; otherwise append with grow.
//        MyObjectSnapshot[] tmp = new MyObjectSnapshot[n + 32];
//        int i = 0;
//
//        for (ClueObject o : chunkMap.values()) {
//            tmp[i++] = toSnap(o);
//        }
//        if (extraFromSavedDataInThisChunk != null) {
//            for (ClueObject o : extraFromSavedDataInThisChunk) {
//                tmp[i++] = toSnap(o);
//            }
//        }
//
//        if (i != tmp.length) {
//            MyObjectSnapshot[] exact = new MyObjectSnapshot[i];
//            System.arraycopy(tmp, 0, exact, 0, i);
//            tmp = exact;
//        }
//        return new ChunkSnapshot(cp.toLong(), revision, tmp);
//    }
//
//    private static MyObjectSnapshot toSnap(ClueObject o) {
//        BlockPos p = o.pos(); // your field
//        return new MyObjectSnapshot(
//                p.asLong(),
//                o.argb(),          // adapt
//                o.kindFlags(),     // adapt
//                o.size()           // adapt
//        );
//    }
//}
