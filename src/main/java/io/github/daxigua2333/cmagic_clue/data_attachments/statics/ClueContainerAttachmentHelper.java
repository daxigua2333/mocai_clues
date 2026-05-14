package io.github.daxigua2333.cmagic_clue.data_attachments.statics;

import io.github.daxigua2333.cmagic_clue.data_attachments.ClueContainer;
import io.github.daxigua2333.cmagic_clue.data_attachments.ClueContainerMap;
import io.github.daxigua2333.cmagic_clue.data_attachments.ModDataAttachmentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.items.IItemHandler;


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
        if (map.containsKey(pos)){
            dropHandlerContents(level, pos, map.getExisting(pos).getInv(chunk));
            map.remove(pos, chunk);
        }
    }

    public static void dropHandlerContents(Level level, BlockPos pos, IItemHandler handler) {
        if (level == null || level.isClientSide() || handler == null) return;

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            // Extract the full stack from the handler (removes it server-side)
            ItemStack removed = handler.extractItem(slot, stack.getCount(), false);
            if (removed.isEmpty()) continue;

            // TODO: config: random offset
            double rx = level.random.nextDouble() * 0.7 + 0.15;
            double ry = level.random.nextDouble() * 0.7 + 0.15;
            double rz = level.random.nextDouble() * 0.7 + 0.15;
            double x = pos.getX() + rx;
            double y = pos.getY() + ry;
            double z = pos.getZ() + rz;

            ItemEntity itement = new ItemEntity(level, x, y, z, removed.copy());
            // set a pickup delay (use default helper or explicit ticks) TODO: config
            itement.setDefaultPickUpDelay(); // or itement.setPickUpDelay(40);
            level.addFreshEntity(itement);
        }
    }
}
