//package io.github.daxigua2333.mocai_clues.data.client.render_backing;
//
//import com.mojang.blaze3d.vertex.ByteBufferBuilder;
//import com.mojang.blaze3d.vertex.MeshData;
//
//public final class OwnedMesh implements AutoCloseable {
//    public final MeshData mesh;
//    private final ByteBufferBuilder backing;
//
//    public OwnedMesh(MeshData mesh, ByteBufferBuilder backing) {
//        this.mesh = mesh;
//        this.backing = backing;
//    }
//
//    @Override
//    public void close() {
//        // MeshData.close() will close its internal Result buffers.
//        try {
//            mesh.close();
//        } finally {
//            backing.close(); // frees the underlying native allocation.
//        }
//    }
//}
