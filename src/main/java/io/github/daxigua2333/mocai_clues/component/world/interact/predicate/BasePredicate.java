package io.github.daxigua2333.mocai_clues.component.world.interact.predicate;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;

public abstract class BasePredicate {
    public abstract InteractEventRegistry.PredicateType type();

    public static final Codec<BasePredicate> CODEC = InteractEventRegistry.PredicateType.CODEC.dispatch(
            "type",
            BasePredicate::type,
            InteractEventRegistry.PredicateType::codec
    );

    public abstract boolean test(InteractEvent.Context context);

    public abstract void onHandle();
}
