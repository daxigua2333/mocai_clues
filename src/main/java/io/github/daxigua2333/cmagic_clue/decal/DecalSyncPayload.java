package io.github.daxigua2333.cmagic_clue.decal;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.data.ModAttachmentRegistry;
import io.github.daxigua2333.cmagic_clue.decal.client.DecalAtlasRegistry;
import io.github.daxigua2333.cmagic_clue.decal.client.DecalRenderer;
import io.github.daxigua2333.cmagic_clue.decal.common.BlockFace;
import io.github.daxigua2333.cmagic_clue.decal.common.DecalDataUnit;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DecalSyncPayload {
    public record PushLayer(ChunkPos chunkPos, BlockFace blockFace,
                            DecalDataUnit dataUnit) implements CustomPacketPayload {
        public static final Type<PushLayer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "decal_push_layer_sync_payload"));
        public static final StreamCodec<ByteBuf, PushLayer> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG.map(ChunkPos::new, ChunkPos::toLong), PushLayer::chunkPos,
                BlockFace.STREAM_CODEC, PushLayer::blockFace,
                DecalDataUnit.STREAM_CODEC, PushLayer::dataUnit,
                PushLayer::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(PushLayer payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player().level() instanceof ClientLevel level) {
                    LevelChunk chunk = level.getChunk(payload.chunkPos.x, payload.chunkPos.z);
                    DecalLayerHolder holder = chunk.getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER);
                    holder.pushLayerFromServerSync(payload.blockFace, payload.dataUnit, DecalAtlasRegistry.ATLAS);

                    DecalRenderer.markDirty(payload.chunkPos);
                }
            });
        }
    }

    public record PopLayer(ChunkPos chunkPos, BlockFace blockFace) implements CustomPacketPayload {
        public static final Type<PopLayer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "decal_pop_layer_sync_payload"));
        public static final StreamCodec<ByteBuf, PopLayer> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG.map(ChunkPos::new, ChunkPos::toLong), PopLayer::chunkPos,
                BlockFace.STREAM_CODEC, PopLayer::blockFace,
                PopLayer::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(PopLayer payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player().level() instanceof ClientLevel level) {
                    LevelChunk chunk = level.getChunk(payload.chunkPos.x, payload.chunkPos.z);
                    DecalLayerHolder holder = chunk.getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER);
                    holder.popLayerFromServerSync(payload.blockFace, DecalAtlasRegistry.ATLAS);

                    DecalRenderer.markDirty(payload.chunkPos);
                }
            });
        }

    }

    public record ClearLayer(ChunkPos chunkPos, BlockFace blockFace) implements CustomPacketPayload {
        public static final Type<ClearLayer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "decal_clear_layer_sync_payload"));
        public static final StreamCodec<ByteBuf, ClearLayer> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG.map(ChunkPos::new, ChunkPos::toLong), ClearLayer::chunkPos,
                BlockFace.STREAM_CODEC, ClearLayer::blockFace,
                ClearLayer::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ClearLayer payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player().level() instanceof ClientLevel level) {
                    LevelChunk chunk = level.getChunk(payload.chunkPos.x, payload.chunkPos.z);
                    DecalLayerHolder holder = chunk.getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER);
                    holder.clearLayerFromServerSync(payload.blockFace, DecalAtlasRegistry.ATLAS);

                    DecalRenderer.markDirty(payload.chunkPos);
                }
            });
        }

    }

    public record UpdateTopLayer(ChunkPos chunkPos, BlockFace blockFace,
                                 DecalDataUnit dataUnit) implements CustomPacketPayload {
        public static final Type<UpdateTopLayer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "decal_update_top_layer_sync_payload"));
        public static final StreamCodec<ByteBuf, UpdateTopLayer> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG.map(ChunkPos::new, ChunkPos::toLong), UpdateTopLayer::chunkPos,
                BlockFace.STREAM_CODEC, UpdateTopLayer::blockFace,
                DecalDataUnit.STREAM_CODEC, UpdateTopLayer::dataUnit,
                UpdateTopLayer::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(UpdateTopLayer payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player().level() instanceof ClientLevel level) {
                    LevelChunk chunk = level.getChunk(payload.chunkPos.x, payload.chunkPos.z);
                    DecalLayerHolder holder = chunk.getData(ModAttachmentRegistry.DECAL_LAYER_HOLDER);
                    holder.updateTopLayerFromServerSync(payload.blockFace, payload.dataUnit, DecalAtlasRegistry.ATLAS);

                    // dont update vbo here because vertex uv doesnt change
                }
            });
        }

    }

    public record ExportAtlas() implements CustomPacketPayload {
        public static final Type<ExportAtlas> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "decal_export_atlas_payload"));
        public static final StreamCodec<ByteBuf, ExportAtlas> STREAM_CODEC = StreamCodec.unit(new ExportAtlas());
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ExportAtlas payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player().level() instanceof ClientLevel level) {
                    DecalAtlasRegistry.ATLAS.export();
                }
            });
        }

    }
}
