package io.github.daxigua2333.mocai_clues.component.world.interact.predicate;

import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;

public class DefaultPredicate extends BasePredicate{
    @Override
    public InteractEventRegistry.PredicateType type() {
        return InteractEventRegistry.PredicateType.DEFAULT;
    }

    public static final MapCodec<FinderHit> CODEC = MapCodec.unit(new FinderHit());

    @Override
    public boolean test(InteractEvent.Context context) {
        return true;
    }

    @Override
    public void onHandle() {

    }
}
