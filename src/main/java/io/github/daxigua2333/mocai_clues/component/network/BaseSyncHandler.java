package io.github.daxigua2333.mocai_clues.component.network;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import net.minecraft.server.level.ServerLevel;

/**
 * Handle sync.
 * When the ClueObject reaches destination,
 * the handler will iterate all component of this Base Class, and invoke handleSync
 * */
public abstract class BaseSyncHandler extends ClueComponent {
    public abstract void handleSync(ServerLevel level);
}
