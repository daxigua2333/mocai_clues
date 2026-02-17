package io.github.daxigua2333.mocai_clues.component.data.discovery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.component.ClueComponent;
import io.github.daxigua2333.mocai_clues.component.ComponentType;
import io.github.daxigua2333.mocai_clues.utils.EnumCodecProvider;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class InteractResult extends ClueComponent {
    public enum ResultType {
        SEND_ITEM,
        SEND_TO_CLUE_BOOK,
//        SEND_MANUAL_CLUE,
//        SEND_FOOTPRINT_CLUE,
        ;

        public static final Codec<ResultType> CODEC = EnumCodecProvider.createCodec(ResultType.class);
    }

    private final EnumSet<ResultType> allowed;

    private InteractResult(EnumSet<ResultType> allowed) {
        this.allowed = allowed;
    }

    public InteractResult() {
        this(EnumSet.noneOf(ResultType.class));
    }

    public InteractResult(ResultType type) {
        this();
        this.setSingle(type);
    }

    public EnumSet<ResultType> getAllowed() {
        return allowed;
    }

    public void setSingle(ResultType type) {
        allowed.clear();
        allowed.add(type);
    }

    public boolean hasResultType(ResultType type) {
        return allowed.contains(type);
    }

    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_RESULT;
    }

    public static final Codec<InteractResult> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResultType.CODEC.listOf().xmap(
                    list -> list.isEmpty() ? EnumSet.noneOf(ResultType.class) : EnumSet.copyOf(list),
                    set -> List.copyOf(set)
            ).fieldOf("set").forGetter(InteractResult::getAllowed)
    ).apply(inst, InteractResult::new));

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        return List.of();
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of();
    }
}
