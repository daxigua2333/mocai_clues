package io.github.daxigua2333.mocai_clues.component.world.interact.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;

public class FirstComeFirstServed extends BasePredicate {
    private int remain;

    public FirstComeFirstServed(int remain) {
        this.remain = remain;
    }

    private int getRemain() {
        return remain;
    }

    @Override
    public InteractEventRegistry.PredicateType type() {
        return InteractEventRegistry.PredicateType.FIRST_COME_FIRST_SERVED;
    }

    @Override
    public boolean test(InteractEvent.Context context) {
        return remain > 0;
    }

    @Override
    public void onHandle() {
        remain--;
    }

    public static final MapCodec<FirstComeFirstServed> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.INT.fieldOf("remain").forGetter(FirstComeFirstServed::getRemain)
    ).apply(inst, FirstComeFirstServed::new));
}
