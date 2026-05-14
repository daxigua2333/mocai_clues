//package io.github.daxigua2333.mocai_clues.mixins;
//
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.MenuProvider;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.Unique;
//
//@Mixin(ServerPlayer.class)
//public abstract class ServerPlayerMixin implements ISilentMenuOpener {
//
//    @Shadow
//    private int containerCounter;
//
//    @Shadow
//    protected abstract void nextContainerCounter();
//
//    //    @Shadow public AbstractContainerMenu containerMenu;
//    @Shadow
//    public abstract void initMenu(AbstractContainerMenu menu);
//
//    @Override
//    @Unique
//    public int mocai_clues$openMenuSilent(MenuProvider provider) {
//        ServerPlayer player = (ServerPlayer) (Object) this;
//
//        // 1. Logic from ServerPlayer#openMenu
//        if (player.containerMenu != player.inventoryMenu) {
//            player.closeContainer();
//        }
//
//        this.nextContainerCounter();
//        AbstractContainerMenu menu = provider.createMenu(this.containerCounter, player.getInventory(), player);
//
//        if (menu == null) return -1;
//
//        // 2. Set the active menu
//        player.containerMenu = menu;
//
//        // 3. Initialize (syncs initial state and adds listeners)
//        this.initMenu(menu);
//
//        // Return the ID so we can send our custom packet
//        return this.containerCounter;
//    }
//}
