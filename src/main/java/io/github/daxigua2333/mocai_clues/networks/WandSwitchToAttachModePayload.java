package io.github.daxigua2333.mocai_clues.networks;

import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.data.common.DataManager;
import io.github.daxigua2333.mocai_clues.data.location.FromSavedData;
import io.github.daxigua2333.mocai_clues.items.ModItemsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.AttachingObject;
import io.github.daxigua2333.mocai_clues.items.components.ModDataComponentsRegistry;
import io.github.daxigua2333.mocai_clues.items.components.WandMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// TODO: route to SD or something...
public record WandSwitchToAttachModePayload(ClueObject object) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WandSwitchToAttachModePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MoCaiClues.MODID, "wand_switch_to_attach_mode_payload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WandSwitchToAttachModePayload> STREAM_CODEC =
            StreamCodec.of(
                    (RegistryFriendlyByteBuf buf, WandSwitchToAttachModePayload payload) -> {
                        ClueObject.STREAM_CODEC.encode(buf, payload.object);
                    },
                    buf -> {
                        return new WandSwitchToAttachModePayload(ClueObject.STREAM_CODEC.decode(buf));
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WandSwitchToAttachModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // update Database
            DataManager.Server.upsert(new FromSavedData(context.player().level()), payload.object());
            // update main hand
            Player player = context.player();
            ItemStack stack = player.getMainHandItem();
            if (stack.is(ModItemsRegistry.CLUE_WAND_ITEM.get())) {
                // update mode
                stack.update(ModDataComponentsRegistry.WAND_MODE.get(), WandMode.CREATE, current -> WandMode.ATTACH);
                player.displayClientMessage(Component.literal("Wand mode: " + WandMode.ATTACH.toString()), true);
                // update attaching
                stack.update(ModDataComponentsRegistry.ATTACHING_OBJECT.get(),
                        new AttachingObject(payload.object().getId()), current -> new AttachingObject(payload.object().getId()));
            } else {
                throw new RuntimeException("Why you are not holding the wand???");
            }

        });

    }
}
