package io.github.daxigua2333.cmagic_clue.decal;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.*;

public class DecalLayerHolder {

    public static int BYTE_PER_PIXEL = 4;  // RGBA
    public static int DYNAMIC_LAYER_SIZE = 16 * 16;

    private final Map<BlockFace, ArrayDeque<DecalDataUnit>> backing;


    public DecalLayerHolder() {
        this.backing = new HashMap<>();
    }

    private DecalLayerHolder(Map<BlockFace, ArrayDeque<DecalDataUnit>> backing) {
        this.backing = new HashMap<>(backing);
    }

    private Map<BlockFace, ArrayDeque<DecalDataUnit>> getBacking() {
        return this.backing;
    }

    public record DecalEntry(BlockFace blockFace, ArrayDeque<DecalDataUnit> deque) {
        public static final Codec<DecalEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        BlockFace.CODEC.fieldOf("blockFace").forGetter(DecalEntry::blockFace),
                        DecalDataUnit.CODEC.listOf().xmap(
                                ArrayDeque::new,
                                List::copyOf
                        ).fieldOf("decal").forGetter(DecalEntry::deque)
                ).apply(instance, DecalEntry::new)
        );
    }

//    private static final Codec<Map<BlockFace, ArrayDeque<DecalDataUnit>>> BACKING_CODEC = Codec.unboundedMap(
//            BlockFace.CODEC,
//            DecalDataUnit.CODEC.listOf().xmap(
//                    ArrayDeque::new,
//                    List::copyOf
//            )
//    );

    private static final Codec<Map<BlockFace, ArrayDeque<DecalDataUnit>>> BACKING_CODEC = DecalEntry.CODEC.listOf().xmap(
            list -> {
                Map<BlockFace, ArrayDeque<DecalDataUnit>> map = new HashMap<>();
                for (DecalEntry entry : list) {
                    map.put(entry.blockFace, entry.deque);
                }
                return map;
            },
            map -> {
                List<DecalEntry> list = new ArrayList<>();
                for (Map.Entry<BlockFace, ArrayDeque<DecalDataUnit>> entry : map.entrySet()) {
                    list.add(new DecalEntry(entry.getKey(), entry.getValue()));
                }
                return list;
            }
    );

    public static final Codec<DecalLayerHolder> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BACKING_CODEC.fieldOf("backing").forGetter(DecalLayerHolder::getBacking)
            ).apply(instance, DecalLayerHolder::new)
    );

    //    public static final StreamCodec<ByteBuf, DecalLayerHolder> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    // This should only be used when syncing because the decoder is client-side specific
    public static StreamCodec<FriendlyByteBuf, DecalLayerHolder> syncCodec(DecalAtlas atlas) {
        return StreamCodec.of(
                (buf, data) -> {
                    buf.writeVarInt(data.getBacking().size());
                    for (var entry : data.getBacking().entrySet()) {
                        BlockFace.STREAM_CODEC.encode(buf, entry.getKey());

                        ArrayDeque<DecalDataUnit> deque = entry.getValue();
                        buf.writeVarInt(deque.size());
                        for (var unit : deque) {
                            DecalDataUnit.STREAM_CODEC.encode(buf, unit);
                        }
                    }
                },
                buf -> {
                    int mapSize = buf.readVarInt();
                    Map<BlockFace, ArrayDeque<DecalDataUnit>> map = new HashMap<>(mapSize);
                    for (int i = 0; i < mapSize; i++) {
                        BlockFace key = BlockFace.STREAM_CODEC.decode(buf);

                        int dequeSize = buf.readVarInt();
                        ArrayDeque<DecalDataUnit> deque = new ArrayDeque<>(dequeSize);
                        for (int j = 0; j < dequeSize; j++) {
                            DecalDataUnit sUnit = DecalDataUnit.STREAM_CODEC.decode(buf);
                            DecalDataUnit cUnit = DecalDataUnit.covertS2C(sUnit, atlas);

                            deque.add(cUnit);
                        }

                        map.put(key, deque);
                    }

                    return new DecalLayerHolder(map);
                }
        );
    }


    // =========== server logic ==============
    // push a static layer
    public void pushLayerAt(ServerLevel level, BlockPos pos, Direction face, ResourceLocation rl) {
        BlockFace blockFace = new BlockFace(pos, face);
        DecalDataUnit dataUnit = new DecalDataUnit(DecalDataUnit.Type.STATIC, rl);
        ChunkPos chunkPos = new ChunkPos(pos);

        ArrayDeque<DecalDataUnit> deque = getBacking().computeIfAbsent(blockFace, k -> new ArrayDeque<>());
        // just simply push static type decal to stack
        deque.addLast(dataUnit);
        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new DecalSyncPayload.PushLayer(chunkPos, blockFace, dataUnit));
    }

    // push a dynamic layer
    public void pushLayerAt(ServerLevel level, BlockPos pos, Direction face, byte[] meta) {
        BlockFace blockFace = new BlockFace(pos, face);
        DecalDataUnit dataUnit = new DecalDataUnit(DecalDataUnit.Type.DYNAMIC, meta);
        ChunkPos chunkPos = new ChunkPos(pos);
        ArrayDeque<DecalDataUnit> deque = getBacking().computeIfAbsent(blockFace, k -> new ArrayDeque<>());

        // if top layer is dynamic, update it; else push a new one
        if (deque.peekLast() != null && deque.peekLast().type() == DecalDataUnit.Type.DYNAMIC) {
            deque.removeLast();
            deque.addLast(dataUnit);
            PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new DecalSyncPayload.UpdateTopLayer(chunkPos, blockFace, dataUnit));

        } else {
            deque.addLast(dataUnit);
            PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new DecalSyncPayload.PushLayer(chunkPos, blockFace, dataUnit));
        }
    }

    // push a one-pixel change
    public void pushOnePixelLayerAt(ServerLevel level, BlockPos pos, Direction face, byte[] pixelData, int startIndex) {
        if (pixelData.length != BYTE_PER_PIXEL)
            throw new RuntimeException("Wrong pixel format. Data length:" + pixelData.length);
        BlockFace blockFace = new BlockFace(pos, face);
        ChunkPos chunkPos = new ChunkPos(pos);
        ArrayDeque<DecalDataUnit> deque = getBacking().computeIfAbsent(blockFace, k -> new ArrayDeque<>());

        // if top layer is dynamic, update it; else push a new one
        if (deque.peekLast() != null && deque.peekLast().type() == DecalDataUnit.Type.DYNAMIC) {
            byte[] meta = (byte[]) deque.peekLast().meta();
            System.arraycopy(pixelData, 0, meta, startIndex, BYTE_PER_PIXEL);

            PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new DecalSyncPayload.UpdateTopLayer(chunkPos, blockFace, deque.peekLast()));

        } else {
            byte[] meta = new byte[BYTE_PER_PIXEL * DYNAMIC_LAYER_SIZE];
            System.arraycopy(pixelData, 0, meta, startIndex, BYTE_PER_PIXEL);

            DecalDataUnit dataUnit = new DecalDataUnit(DecalDataUnit.Type.DYNAMIC, meta);
            deque.addLast(dataUnit);
            PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new DecalSyncPayload.PushLayer(chunkPos, blockFace, dataUnit));
        }
    }

    public void popLayerAt(ServerLevel level, BlockPos pos, Direction face) {
        BlockFace blockFace = new BlockFace(pos, face);
        ChunkPos chunkPos = new ChunkPos(pos);

        ArrayDeque<DecalDataUnit> deque = getBacking().computeIfAbsent(blockFace, k -> new ArrayDeque<>());
        deque.removeLast();
        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new DecalSyncPayload.PopLayer(chunkPos, blockFace));

    }

    public void clearLayerAt(ServerLevel level, BlockPos pos, Direction face) {
        BlockFace blockFace = new BlockFace(pos, face);
        ChunkPos chunkPos = new ChunkPos(pos);

        if (!getBacking().containsKey(blockFace)) return;
        getBacking().get(blockFace).clear();
        PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, new DecalSyncPayload.ClearLayer(chunkPos, blockFace));
    }

    // ========== client logic ================
    public void addToMesh(BufferBuilder builder, float layerOffset) {
        for (var entry : backing.entrySet()) {
            Vector3f[] coordinates = entry.getKey().getFaceVertices(layerOffset);

            for (DecalDataUnit unit : entry.getValue()) {
                if (unit.type() != DecalDataUnit.Type.CLIENT) throw new RuntimeException();
                if (unit.meta() instanceof AtlasRegion region) {
                    // add position-uv vertex
                    builder.addVertex(coordinates[0]).setUv(region.getU0(), region.getV0());
                    builder.addVertex(coordinates[1]).setUv(region.getU0(), region.getV1());
                    builder.addVertex(coordinates[2]).setUv(region.getU1(), region.getV1());
                    builder.addVertex(coordinates[3]).setUv(region.getU1(), region.getV0());
                }
            }

        }
    }

    public void clientClose(DecalAtlas atlas) {
        for (var entry : getBacking().entrySet()) {
            for (DecalDataUnit unit : entry.getValue()) {
                atlas.freeDynamic((AtlasRegion) unit.meta());
            }
        }
    }

    public void pushLayerFromServerSync(BlockFace blockFace, DecalDataUnit sUnit, DecalAtlas atlas) {
        ArrayDeque<DecalDataUnit> deque = getBacking().computeIfAbsent(blockFace, k -> new ArrayDeque<>());
        deque.addLast(DecalDataUnit.covertS2C(sUnit, atlas));
    }

    public void popLayerFromServerSync(BlockFace blockFace, DecalAtlas atlas) {
        ArrayDeque<DecalDataUnit> deque = getBacking().get(blockFace);  // just let it throw if it s out of sync
        DecalDataUnit cUnit = deque.removeLast();
        atlas.freeDynamic((AtlasRegion) cUnit.meta());
    }

    public void clearLayerFromServerSync(BlockFace blockFace, DecalAtlas atlas) {
        if (!getBacking().containsKey(blockFace)) return;
        ArrayDeque<DecalDataUnit> deque = getBacking().get(blockFace);
        for (DecalDataUnit unit : deque) {
            atlas.freeDynamic((AtlasRegion) unit.meta());
        }
        deque.clear();
    }

    public void updateTopLayerFromServerSync(BlockFace blockFace, DecalDataUnit sUnit, DecalAtlas atlas) {
        ArrayDeque<DecalDataUnit> deque = getBacking().get(blockFace);  // just let it throw if it s out of sync
        DecalDataUnit cUnit = deque.peekLast();
        atlas.updateDynamic((AtlasRegion) cUnit.meta(), (byte[]) sUnit.meta());

    }
}
