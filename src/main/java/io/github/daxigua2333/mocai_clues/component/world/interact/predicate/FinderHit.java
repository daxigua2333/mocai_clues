//package io.github.daxigua2333.mocai_clues.component.world.interact.predicate;
//
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.MapCodec;
//import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEvent;
//import io.github.daxigua2333.mocai_clues.component.world.interact.InteractEventRegistry;
//
//public class FinderHit extends BasePredicate{
//
//    @Override
//    public InteractEventRegistry.PredicateType type() {
//        return InteractEventRegistry.PredicateType.FINDER_HIT;
//    }
//
//    public static final MapCodec<FinderHit> CODEC = MapCodec.unit(new FinderHit());
//
//    @Override
//    public boolean test(InteractEvent.Context context) {
//        if (context.entryType() == InteractEventRegistry.EntryType.RAY_TRACE) {
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void onHandle() {
//
//    }
//}
