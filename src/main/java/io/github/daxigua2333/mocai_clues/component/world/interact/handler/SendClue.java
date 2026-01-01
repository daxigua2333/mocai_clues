package io.github.daxigua2333.mocai_clues.component.world.interact.handler;

import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.DetailData;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class SendClue extends BaseHandler {
    @Override
    public InteractEventRegistry.HandlerType type() {
        return InteractEventRegistry.HandlerType.SEND_CLUE;
    }

    public static final MapCodec<SendClue> CODEC = MapCodec.unit(new SendClue());

    @Override
    public void handle(InteractEvent.Context context) {
        Player player = context.player();
        if (player == null) return;
        if (player.level().isClientSide) return;
        MoCaiClues.LOGGER.debug("111: {}", context.entryType());

        ClueObject object = context.object();
        DetailData compo = object.getComponent(ComponentType.DETAIL_DATA);
        if (compo == null) return;
        player.sendSystemMessage(Component.literal(compo.getName()));
        compo.getDetails().forEach(s -> {
            player.sendSystemMessage(Component.literal(s));
        });
    }
}
