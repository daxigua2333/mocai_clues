package io.github.daxigua2333.cmagic_clue.component.data.discovery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ComponentType;
import io.github.daxigua2333.cmagic_clue.component.data.EnumSelectorComponent;
import io.github.daxigua2333.cmagic_clue.guis.widget.editable.EditBoxWithBacking;
import io.github.daxigua2333.cmagic_clue.utils.EnumCodecProvider;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InteractResult extends EnumSelectorComponent<InteractResult.ResultType> {
    public enum ResultType {
        SEND_ITEM,
        SEND_TO_CLUE_BOOK,
        SEND_TO_CHAT_BOX,
//        SEND_MANUAL_CLUE,
//        SEND_FOOTPRINT_CLUE,
        ;

        public static final Codec<ResultType> CODEC = EnumCodecProvider.createCodec(ResultType.class);
    }

    private UUID clueBookUUID;

    private InteractResult(EnumSet<ResultType> allowed, EnumSet<ResultType> enabled, UUID clueBookUUID) {
        super(ResultType.class, allowed, enabled);
        this.clueBookUUID = clueBookUUID;
    }

    public InteractResult(EnumSet<ResultType> allowed, EnumSet<ResultType> enabled) {
        this(allowed, enabled, UUID.randomUUID());
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


    public UUID getClueBookUUID() {
        return clueBookUUID;
    }


    @Override
    public ComponentType type() {
        return ComponentType.INTERACT_RESULT;
    }

    public static final Codec<InteractResult> CODEC = RecordCodecBuilder.create(inst -> {
        Codec<EnumSet<ResultType>> setCodec = EnumSelectorComponent.getSetCodec(ResultType.class, ResultType.CODEC);
        return EnumSelectorComponent.commonFields(inst, setCodec)
                .and(UUIDUtil.CODEC.optionalFieldOf("clueBookUUID").forGetter(obj -> Optional.of(obj.getClueBookUUID())))
                .apply(inst, (allowed, enabled, idOpt) -> new InteractResult(allowed, enabled, idOpt.orElse(UUID.randomUUID())));
    });


    @Override
    protected String getHeaderKey() {
        return CMagicClue.MODID + ".screen.interact_result";
    }

    @Override
    protected List<AbstractWidget> getEditableWidget(ResultType type) {
        return switch (type) {
            case SEND_TO_CLUE_BOOK -> List.of(
                    EditBoxWithBacking.uuidBox(0, 0, 0, 20,
                            () -> clueBookUUID, id -> clueBookUUID = id,
                            v -> true, Component.literal("UUID"))
            );
            case SEND_ITEM, SEND_TO_CHAT_BOX -> List.of();
        };
    }
}
