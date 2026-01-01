package io.github.daxigua2333.mocai_clues.component.world.interact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ClueObject;
import io.github.daxigua2333.mocai_clues.component.world.interact.handler.BaseHandler;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.BasePredicate;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class InteractEvent {
//    public abstract EntryType entryType();
    private final InteractEventRegistry.EntryType entryType;
    private final BasePredicate predicate;
    private final BaseHandler handler;

    public InteractEvent(InteractEventRegistry.EntryType entryType, BasePredicate predicate, BaseHandler handler) {
        this.entryType = entryType;
        this.predicate = predicate;
        this.handler = handler;
    }

    public BaseHandler getHandler() {
        return handler;
    }
    public BasePredicate getPredicate() {
        return predicate;
    }
    public InteractEventRegistry.EntryType getEntryType() {
        return entryType;
    }

    public static final Codec<InteractEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            InteractEventRegistry.EntryType.CODEC.fieldOf("entryType").forGetter(InteractEvent::getEntryType),
            BasePredicate.CODEC.fieldOf("predicate").forGetter(InteractEvent::getPredicate),
            BaseHandler.CODEC.fieldOf("handler").forGetter(InteractEvent::getHandler)
    ).apply(instance, InteractEvent::new));


    public record Context(
            InteractEventRegistry.EntryType entryType,
            @Nullable Player player,
            ClueObject object
    ) {
        // Builder pattern
        private Context(Builder builder) {
            this(builder.entryType, builder.player, builder.object);
        }
        public static class Builder {
            private InteractEventRegistry.EntryType entryType;
            private @Nullable Player player;
            private ClueObject object;
            public Builder(InteractEventRegistry.EntryType entry){
                this.entryType = entry;
            }
            public Builder player(Player player) {
                this.player = player;
                return this;
            }
            public Builder object(ClueObject object) {
                this.object = object;
                return this;
            }
            public Context build() {
                return new Context(this);
            }
        }
    }
    public void onHandle(Context context) {
        // TODO: actually EntryType is just another predicate, so this is a predicate Assembler, which is a rather complex TODO...
        if (entryType.equals(context.entryType()) && predicate.test(context)) {
            handler.handle(context);
            predicate.onHandle();
        }
    }
}
