package io.github.daxigua2333.mocai_clues.mixins;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.statics.FinderHitResultTicker;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Map;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "copy", at = @At("RETURN"))
    private void copy(CallbackInfoReturnable<ItemStack> cir) {
        if (cir.getReturnValue().getItem() == ModItemsRegistry.CLUE_FINDER_ITEM.get()) {
            ItemStack newthis = (ItemStack) (Object) this;
            var map = FinderHitResultTicker.prevMainHoldFinderCopyMap;
            if (!map.containsKey(newthis)){
                map.put(newthis, new ArrayList<>());
            }
            map.get(newthis).add(cir.getReturnValue());
        }
    }
}
