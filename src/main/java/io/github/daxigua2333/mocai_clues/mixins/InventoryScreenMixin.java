//package io.github.daxigua2333.mocai_clues.mixins;
//
//import io.github.daxigua2333.mocai_clues.guis.ClueBookScreen;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.components.Button;
//import net.minecraft.client.gui.components.Renderable;
//import net.minecraft.client.gui.components.events.GuiEventListener;
//import net.minecraft.client.gui.narration.NarratableEntry;
//import net.minecraft.client.gui.screens.inventory.InventoryScreen;
//import net.minecraft.network.chat.Component;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(InventoryScreen.class)
//public abstract class InventoryScreenMixin {
//    @Shadow public int leftPos;
//    @Shadow public int topPos;
////    @Shadow protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);
//    @Shadow
//    protected abstract <T extends GuiEventListener & Renderable & NarratableEntry>
//    T addRenderableWidget(T widget);
//
//    @Inject(method = "init", at = @At("RETURN"))
//    private void init(CallbackInfo ci) {
//        // Top-left corner of the INVENTORY GUI background:
////        int left = getGuiLeft();
////        int top  = getGuiTop();
//        // Choose a position. These coordinates are relative to the inventory GUI.
//        // If you put it at left+2/top+2 it may overlap armor slots, so many mods place it just outside:
//        int x = leftPos - 20;   // just to the left of the GUI
//        int y = topPos + 4;     // near the top
//        addRenderableWidget(
//            Button.builder(Component.literal("My"), btn -> {
//                Minecraft.getInstance().setScreen(new ClueBookScreen());
//            })
//            .pos(x, y)
//            .size(20, 20)
//            .build()
//        );
//
//    }
//}
