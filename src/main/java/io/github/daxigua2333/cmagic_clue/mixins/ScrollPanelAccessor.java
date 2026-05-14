package io.github.daxigua2333.cmagic_clue.mixins;

import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScrollPanel.class)
public interface ScrollPanelAccessor {
    @Accessor("left")
    @Mutable
    void setX(int x);

    @Accessor("top")
    @Mutable
    void setY(int y);

}
