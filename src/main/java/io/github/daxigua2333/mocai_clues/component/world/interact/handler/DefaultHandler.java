package io.github.daxigua2333.mocai_clues.component.world.interact.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;

public class DefaultHandler extends BaseHandler{
    @Override
    public InteractEventRegistry.HandlerType type() {
        return InteractEventRegistry.HandlerType.DEFAULT;
    }

    public static final MapCodec<DefaultHandler> CODEC = MapCodec.unit(new DefaultHandler());

    @Override
    public void handle(InteractEvent.Context context) {

    }
}
