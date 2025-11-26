package io.github.daxigua2333.mocai_clues.footprints.Events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;

public class BlockBecameNonAirEvent extends Event {
    private final Level level;
    private final BlockPos pos;
    private final BlockState oldState;
    private final BlockState newState;

    public BlockBecameNonAirEvent(Level level, BlockPos pos,
                                  BlockState oldState, BlockState newState) {
        this.level = level;
        this.pos = pos;
        this.oldState = oldState;
        this.newState = newState;
    }

    public Level getLevel()     { return level; }
    public BlockPos getPos()    { return pos; }
    public BlockState getOld()  { return oldState; }
    public BlockState getNew()  { return newState; }
}