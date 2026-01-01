package io.github.daxigua2333.mocai_clues.component.world.interact.handler;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;

public abstract class BaseHandler {
    public abstract InteractEventRegistry.HandlerType type();

    public static final Codec<BaseHandler> CODEC = InteractEventRegistry.HandlerType.CODEC.dispatch(
            "type",
            BaseHandler::type,
            InteractEventRegistry.HandlerType::codec
    );

    public abstract void handle(InteractEvent.Context context);
}
