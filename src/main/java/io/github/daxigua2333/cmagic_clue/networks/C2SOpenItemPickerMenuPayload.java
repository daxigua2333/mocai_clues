package io.github.daxigua2333.cmagic_clue.networks;

import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.guis.whitelist.WhitelistMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SOpenItemPickerMenuPayload(ItemStack itemStack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<C2SOpenItemPickerMenuPayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(CMagicClue.MODID, "c2s_open_item_picker_menu_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SOpenItemPickerMenuPayload> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, C2SOpenItemPickerMenuPayload::itemStack,
            C2SOpenItemPickerMenuPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final C2SOpenItemPickerMenuPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack stack = payload.itemStack();
                ItemStackHandler tempHandler = new ItemStackHandler(1);
                tempHandler.setStackInSlot(0, stack);
                MenuProvider provider = new SimpleMenuProvider(
                        (id, playerInv, p) -> new WhitelistMenu(id, playerInv, tempHandler),
                        Component.translatable(CMagicClue.MODID + ".screen.item_clue")
                );

//                player.openMenu(provider, buf -> ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack));
                player.openMenu(provider);
            }
//            if (context.player().level().isClientSide()) return;
//            ServerPlayer player = (ServerPlayer) context.player();
//            int windowId = ((ISilentMenuOpener) player).mocai_clues$openMenuSilent(provider);
//
//            if (windowId != -1) {
//                // Send custom packet to client instead of vanilla one
//                PacketDistributor.sendToPlayer(player, new S2COpenItemPickerMenuPayload(windowId));
//            }
        });
    }
}
