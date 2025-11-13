package io.github.daxigua2333.mocai_clues.data_attachments;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;


public final class ClueContainerAttachmentHelper {
    private ClueContainerAttachmentHelper() {}

    public static Boolean containsKey(final Level level, final BlockPos pos) {
        final LevelChunk chunk = level.getChunkAt(pos);
        ClueContainerMap map = chunk.getData(ModDataAttachmentRegistry.CLUE_CONTAINER_MAP.get());
        return map.containsKey(pos);
    }

    public static ClueContainer getOrCreate(Level level, BlockPos pos){
        LevelChunk chunk = level.getChunkAt(pos);
        ClueContainerMap map = chunk.getData(ModDataAttachmentRegistry.CLUE_CONTAINER_MAP.get());
        if (map.containsKey(pos)){
            return map.getExisting(pos);
        }else{
            ClueContainer container = new ClueContainer();
            map.put(pos, container, chunk);
            return container;
        }
    }

    public static void put(Level level, BlockPos pos, ClueContainer container){
        LevelChunk chunk = level.getChunkAt(pos);
        ClueContainerMap map = chunk.getData(ModDataAttachmentRegistry.CLUE_CONTAINER_MAP.get());
        map.put(pos, container, chunk);
    }

    public static void remove(Level level, BlockPos pos){
        LevelChunk chunk = level.getChunkAt(pos);
        ClueContainerMap map = chunk.getData(ModDataAttachmentRegistry.CLUE_CONTAINER_MAP.get());
        map.remove(pos, chunk);
    }
}
