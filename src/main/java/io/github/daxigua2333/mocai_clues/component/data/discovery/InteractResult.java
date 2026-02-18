package io.github.daxigua2333.mocai_clues.component.data.discovery;

import com.mojang.serialization.Codec;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.component.data.EnumSelectorComponent;
import io.github.daxigua2333.mocai_clues.utils.EnumCodecProvider;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.EnumSet;
import java.util.List;

public class InteractResult extends EnumSelectorComponent<InteractResult.ResultType> {
    public enum ResultType {
        SEND_ITEM,
        SEND_TO_CLUE_BOOK,
//        SEND_MANUAL_CLUE,
//        SEND_FOOTPRINT_CLUE,
        ;

        public static final Codec<ResultType> CODEC = EnumCodecProvider.createCodec(ResultType.class);
    }


    private InteractResult(EnumSet<ResultType> allowed, EnumSet<ResultType> enabled) {
        super(ResultType.class, allowed, enabled);
    }

    /**
     * Default init must specify `allowed`
     */
    public InteractResult(EnumSet<ResultType> allowed) {
        this(allowed, EnumSet.noneOf(ResultType.class));
    }

    /**
     * Simplified init of Single Result
     */
    public InteractResult(ResultType type) {
        this(EnumSet.of(type));
        enable(type);
    }

    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_RESULT;
    }

    public static final Codec<InteractResult> CODEC = createCodec(ResultType.class, ResultType.CODEC, InteractResult::new);

    @Override
    protected String getHeaderKey() {
        return MoCaiClues.MODID + ".screen.interact_result";
    }

    @Override
    protected List<AbstractWidget> getEditableWidget(ResultType type) {
        return List.of();
    }
}
