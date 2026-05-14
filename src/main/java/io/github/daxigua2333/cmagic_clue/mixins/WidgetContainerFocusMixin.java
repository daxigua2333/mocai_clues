package io.github.daxigua2333.cmagic_clue.mixins;

import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(AbstractContainerEventHandler.class)
public class WidgetContainerFocusMixin {
    @Shadow
    private @Nullable GuiEventListener focused;

    @Inject(method = "setFocused", at = @At("HEAD"), cancellable = true)
    private void setFocused(@Nullable GuiEventListener listener, CallbackInfo ci) {
        if (focused == listener) {  // compares reference
            ci.cancel();
        }
    }
}
