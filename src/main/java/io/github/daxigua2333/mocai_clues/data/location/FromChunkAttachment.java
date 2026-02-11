package io.github.daxigua2333.mocai_clues.data.location;

import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.ModAttachmentRegistry;
import io.github.daxigua2333.mocai_clues.data.ObjectHolder;
import io.github.daxigua2333.mocai_clues.data.location.factory.FromChunkAttachmentSerializable;
import io.github.daxigua2333.mocai_clues.data.location.factory.ISerializableLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class FromChunkAttachment extends BaseRuntimeLocation {
    private final LevelChunk chunk;

    public FromChunkAttachment(LevelChunk chunk) {
        this.chunk = chunk;
    }

    public FromChunkAttachment(Level level, ChunkPos chunkPos) {
        this(level.getChunk(chunkPos.x, chunkPos.z));
    }

    public FromChunkAttachment(Level level, BlockPos pos) {
        this(level.getChunkAt(pos));
    }

//    @Override
//    public Type type() {
//        return Type.CHUNK;
//    }

    @Override
    public ObjectHolder<ClueObject> getHolder() {
        return chunk.getData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);
    }

    @Override
    public void markDirty() {
        chunk.setUnsaved(true);
        chunk.syncData(ModAttachmentRegistry.CLUE_OBJECT_HOLDER);  // TODO: lazy sync
    }

    @Override
    public ISerializableLocation getSerializable() {
        return new FromChunkAttachmentSerializable(chunk.getPos());
    }


}
