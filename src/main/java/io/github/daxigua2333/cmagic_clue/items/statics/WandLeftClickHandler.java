package io.github.daxigua2333.cmagic_clue.items.statics;

import io.github.daxigua2333.cmagic_clue.Config;
import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.items.ModItemsRegistry;
import io.github.daxigua2333.cmagic_clue.networks.LeftButtonPressedPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@OnlyIn(value = Dist.CLIENT)
@EventBusSubscriber(modid = CMagicClue.MODID, value = Dist.CLIENT)
public class WandLeftClickHandler {

    @SubscribeEvent
    public static void onMouse(InputEvent.MouseButton.Pre event){
        if(event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;

        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(Minecraft.getInstance().screen != null) return;
        if(player.getMainHandItem().getItem() != ModItemsRegistry.CLUE_WAND_ITEM.get()) return;

        if (event.getAction() == GLFW.GLFW_PRESS){
            PacketDistributor.sendToServer(new LeftButtonPressedPayload());
        }else if (event.getAction() == GLFW.GLFW_RELEASE){
            // TODO: send release packet if needed
        }
    }


    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if(!event.getLevel().isClientSide()) {
//            handleEvent(event);
            if (event.getItemStack().getItem() == ModItemsRegistry.CLUE_WAND_ITEM.get()) {
                event.setCanceled(Config.COMMON.WAND_LEFT_CLICK_CANCEL.get());
            }
        }
    }
}
