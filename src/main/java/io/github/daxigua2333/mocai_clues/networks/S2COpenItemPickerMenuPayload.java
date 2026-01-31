//package io.github.daxigua2333.mocai_clues.networks;
//
//import io.github.daxigua2333.mocai_clues.MoCaiClues;
//import io.github.daxigua2333.mocai_clues.guis.ClueInventoryMenu;
//import io.github.daxigua2333.mocai_clues.guis.ClueInventoryScreen;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.codec.ByteBufCodecs;
//import net.minecraft.network.codec.StreamCodec;
//import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//import net.minecraft.resources.ResourceLocation;
//import net.neoforged.neoforge.network.handling.IPayloadContext;
//
//public record S2COpenItemPickerMenuPayload(int windowId) implements CustomPacketPayload {
//    public static final Type<S2COpenItemPickerMenuPayload> TYPE = new Type<>(
//            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "s2c_open_item_picker_menu_payload"));
//
//    public static final StreamCodec<ByteBuf, S2COpenItemPickerMenuPayload> STREAM_CODEC = StreamCodec.composite(
//            ByteBufCodecs.VAR_INT, S2COpenItemPickerMenuPayload::windowId,
//            S2COpenItemPickerMenuPayload::new
//    );
//
//    @Override
//    public Type<? extends CustomPacketPayload> type() {
//        return TYPE;
//    }
//
//    public static void handle(final S2COpenItemPickerMenuPayload payload, final IPayloadContext context) {
//        context.enqueueWork(() -> {
//            Minecraft mc = Minecraft.getInstance();
//
//            // 1. Create the Client Menu
//            // This must use the same Window ID sent from the server!
//            ClueInventoryMenu menu = new ClueInventoryMenu(
//                    payload.windowId(),
//                    mc.player.getInventory()
//            );
//            // 2. Create the Screen
//            ClueInventoryScreen screen = new ClueInventoryScreen(menu, mc.player.getInventory(), Component.literal("Nested"));
//            // 3. IMPORTANT: Set the player's active container menu on the client
//            // If you don't do this, clicking slots won't work properly
//            mc.player.containerMenu = menu;
//            // 4. Push the layer
//            // This adds it to the Screen stack. NeoForge handles the rendering
//            // and input delegation for layers.
//            mc.pushGuiLayer(screen);
//        });
//    }
//}
