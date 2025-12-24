//package io.github.daxigua2333.mocai_clues.data.client.render_backing;
//
//import io.github.daxigua2333.mocai_clues.data.client.render_backing.pass.PassKind;
//
//import java.util.EnumMap;
//
//public record ChunkCpuBuildResult(
//        long chunkKey,
//        int revision,
//        EnumMap<PassKind, OwnedMesh> meshes
//) implements AutoCloseable {
//
//    @Override
//    public void close() {
//        for (OwnedMesh m : meshes.values()) {
//            m.close();
//        }
//    }
//}
