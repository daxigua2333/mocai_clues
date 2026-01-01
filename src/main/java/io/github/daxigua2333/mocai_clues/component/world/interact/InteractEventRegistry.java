package io.github.daxigua2333.mocai_clues.component.world.interact;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.daxigua2333.mocai_clues.component.world.interact.handler.BaseHandler;
import io.github.daxigua2333.mocai_clues.component.world.interact.handler.DefaultHandler;
import io.github.daxigua2333.mocai_clues.component.world.interact.handler.PlaySound;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.BasePredicate;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.DefaultPredicate;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.FinderHit;
import io.github.daxigua2333.mocai_clues.component.world.interact.predicate.FirstComeFirstServed;

public class InteractEventRegistry {

    public enum EntryType{
        MOVEMENT, CLICK, RAY_TRACE;
    }

    public enum HandlerType {
        DEFAULT,
        PLAY_SOUND,
//        CLUE_SENDER,
//        GIVE_ITEM,
        ;
        public static final Codec<HandlerType> CODEC =
                Codec.STRING.xmap(HandlerType::valueOf, Enum::name);

        public MapCodec<? extends BaseHandler> codec() {
            return switch (this) {
                case DEFAULT -> DefaultHandler.CODEC;
                case PLAY_SOUND -> PlaySound.CODEC;
            };
        }
    }

    public enum PredicateType {
        DEFAULT,
        FIRST_COME_FIRST_SERVED,
        FINDER_HIT,
//        SPECIFIC_PLAYER,
        // SPECIFIC_GROUP, RESTORE_BY_TIME,
        ;
        public static final Codec<PredicateType> CODEC =
                Codec.STRING.xmap(PredicateType::valueOf, Enum::name);

        public MapCodec<? extends BasePredicate> codec() {
            return switch (this) {
                case DEFAULT -> DefaultPredicate.CODEC;
                case FIRST_COME_FIRST_SERVED -> FirstComeFirstServed.CODEC;
                case FINDER_HIT -> FinderHit.CODEC;
            };
        }

    }

}


