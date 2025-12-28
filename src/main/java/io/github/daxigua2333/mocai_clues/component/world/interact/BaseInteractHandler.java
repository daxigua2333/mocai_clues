package io.github.daxigua2333.mocai_clues.component.world.interact;

import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public abstract class BaseInteractHandler extends ClueComponent {
    public record Context(
            @Nullable Player player
    ) {}
    public abstract void onHandle(Context context);
}
