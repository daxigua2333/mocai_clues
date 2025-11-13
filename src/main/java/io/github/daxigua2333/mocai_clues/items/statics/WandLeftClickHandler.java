package io.github.daxigua2333.mocai_clues.items.statics;

import io.github.daxigua2333.mocai_clues.Config;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.networks.LeftButtonPressedPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MoCaiClues.MODID, value = Dist.CLIENT)
public class WandLeftClickHandler {

    @SubscribeEvent
    public static void onMouse(InputEvent.MouseButton.Pre event){
        if(event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return;
        if(Minecraft.getInstance().screen != null) return;
        if(player.getMainHandItem().getItem() != ModItemsRegistry.CLUE_WAND_ITEM.get()) return;

        if (event.getAction() == GLFW.GLFW_PRESS){
            PacketDistributor.sendToServer(new LeftButtonPressedPayload());
        }else if (event.getAction() == GLFW.GLFW_RELEASE){
            // TODO: send release packet if needed
        }
    }

//    @SubscribeEvent
//    public static void onClientTick(ClientTickEvent event) {
//        if (event.phase != ClientTickEvent.Phase.END) return;
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.player == null) {
//            prevAttackDown = false;
//            return;
//        }
//
//        boolean attackDown = mc.options.keyAttack.isDown();
//        // rising edge: key is down now but wasn't last tick
//        if (attackDown && !prevAttackDown) {
//            ItemStack stack = mc.player.getMainHandItem();
//            if (stack.getItem() == ModItems.CLUE_WAND_ITEM.get()) {
//
//            }
//        }
//        prevAttackDown = attackDown;
//    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if(!event.getLevel().isClientSide()) {
//            handleEvent(event);
            if (event.getItemStack().getItem() == ModItemsRegistry.CLUE_WAND_ITEM.get()) {
                event.setCanceled(Config.WAND_LEFT_CLICK_CANCEL.get());
            }
        }
    }

//    @SubscribeEvent
//    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
//        if(!event.getLevel().isClientSide()) {
//            handleEvent(event);
//        }
//    }

//    private static void handleEvent(PlayerInteractEvent event){
//        Player player = event.getEntity();
//        ItemStack stack = player.getMainHandItem();
//        if (stack.getItem() == ModItems.CLUE_WAND_ITEM.get()) {
//            BlockPos pos = event.getPos();
//            Level level = player.level();
//
//            // custom behaviour on server
//            level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 1f, 1f);
//            // update(...) will set the component (creates it if needed) and returns the old/updated value
//            stack.update(ModDataComponents.WAND_MODE.get(), WandMode.CREATE, current -> {
//                // If the component was missing current could be null - guard
//                WandMode cur = current == null ? WandMode.CREATE : current;
//                return cur.next();
//            });
//
//            WandMode newMode = stack.getOrDefault(ModDataComponents.WAND_MODE.get(), WandMode.CREATE);
//
//            // show simple chat feedback (server -> client message to the player)
//            player.displayClientMessage(Component.literal("Wand mode: " + newMode.toString()), true);
//        }
//    }
}
