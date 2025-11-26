package io.github.daxigua2333.mocai_clues.mixins;

import io.github.daxigua2333.mocai_clues.footprints.Events.BlockBecameAirEvent;
import io.github.daxigua2333.mocai_clues.footprints.Events.BlockBecameNonAirEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(method = "onBlockStateChange", at = @At("HEAD"))
    private void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        // Ignore non-changes
        if (oldState == newState) {
            return;
        }

        boolean oldAir = oldState.isAir();
        boolean newAir = newState.isAir();

        if (!oldAir && newAir) {  // non-air -> air
            NeoForge.EVENT_BUS.post(
                new BlockBecameAirEvent((Level)(Object)this, pos.immutable(), oldState, newState)
            );
        } else if (oldAir && !newAir) {  // air -> non-air (includes water, lava, etc.)
            NeoForge.EVENT_BUS.post(
                new BlockBecameNonAirEvent((Level)(Object)this, pos.immutable(), oldState, newState)
            );
        }
    }
}
