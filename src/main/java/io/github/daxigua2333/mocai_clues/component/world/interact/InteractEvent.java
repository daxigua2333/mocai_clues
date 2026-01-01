package io.github.daxigua2333.mocai_clues.component.world.interact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.world.interact.handler.BaseHandler;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.BasePredicate;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class InteractEvent {
//    public abstract EntryType entryType();
    private final BasePredicate predicate;
    private final BaseHandler handler;

    public InteractEvent(BasePredicate predicate, BaseHandler handler) {
        this.handler = handler;
        this.predicate = predicate;
    }

    public BaseHandler getHandler() {
        return handler;
    }
    public BasePredicate getPredicate() {
        return predicate;
    }

    public static final Codec<InteractEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BasePredicate.CODEC.fieldOf("predicate").forGetter(InteractEvent::getPredicate),
            BaseHandler.CODEC.fieldOf("handler").forGetter(InteractEvent::getHandler)
    ).apply(instance, InteractEvent::new));


    public record Context(
            InteractEventRegistry.EntryType entryType,
            @Nullable Player player
//            ClueObject object
    ) {}
    public void onHandle(Context context) {
        if (predicate.test(context)) {
            handler.handle(context);
            predicate.onHandle();
        }
    }
}
