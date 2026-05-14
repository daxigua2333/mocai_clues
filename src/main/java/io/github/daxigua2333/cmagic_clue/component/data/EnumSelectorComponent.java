package io.github.daxigua2333.cmagic_clue.component.data;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.component.ClueComponent;
import io.github.daxigua2333.cmagic_clue.guis.widget.editable.CollapsibleCheckbox;
import io.github.daxigua2333.cmagic_clue.guis.widget.uneditable.ScaledTextRow;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiFunction;

public abstract class EnumSelectorComponent<E extends Enum<E>> extends ClueComponent {
    protected final EnumSet<E> enabled;
    protected final EnumSet<E> allowed;
    protected final Class<E> enumClass;

    protected EnumSelectorComponent(Class<E> enumClass, EnumSet<E> allowed, EnumSet<E> enabled) {
        this.enumClass = enumClass;
        this.allowed = allowed;
        this.enabled = enabled;
    }

    // --- Logic API ---

    public EnumSet<E> getEnabled() {
        return enabled;
    }

    public EnumSet<E> getAllowed() {
        return allowed;
    }

    public boolean isEnabled(E type) {
        return enabled.contains(type);
    }

    public boolean isAllowed(E type) {
        return allowed.contains(type);
    }

    public void enable(E type) {
        if (allowed.contains(type)) enabled.add(type);
    }

    public void disable(E type) {
        enabled.remove(type);
    }

//    public void enableSingle(E type) {
//        enabled.clear();
//        enable(type);
//    }
//
//    @Nullable
//    public E getSingleEnabled() {
//        return enabled.stream().findFirst().orElse(null);
//    }

    // --- UI Hooks ---

    protected abstract String getHeaderKey();

    protected String getEnumTranslationKey(E value) {
        return CMagicClue.MODID + ".enum." + value.toString();
    }

    protected abstract List<AbstractWidget> getEditableWidget(E type);

    // --- UI Implementation ---

    @Nullable
    @Override
    public List<AbstractWidget> getEditable(Runnable markDirty) {
        if (allowed.isEmpty()) return List.of();

        List<AbstractWidget> result = new ArrayList<>();
        result.add(new ScaledTextRow(0, 0, 0, 0, Component.translatable(getHeaderKey()), 1.1f));

        for (E type : allowed) {
            result.add(new CollapsibleCheckbox(0, 0, 0,
                            Component.translatable(getEnumTranslationKey(type)),
                            isEnabled(type),
                            checked -> {
                                if (checked) enable(type);
                                else disable(type);
//                        markDirty.run();
                            },
                            getEditableWidget(type))
            );
        }
        return result;
    }

    @Nullable
    @Override
    public List<AbstractWidget> getUneditable() {
        return List.of(); // Keep consistent with your current empty impl
    }

    // --- Codec Helper ---

    // ... Existing fields (allowed, enabled) and Getters ...
    // 1. Extract the Set Codec logic to a static helper
    protected static <E extends Enum<E>> Codec<EnumSet<E>> getSetCodec(Class<E> enumClass, Codec<E> enumCodec) {
        return enumCodec.listOf().xmap(
                list -> list.isEmpty() ? EnumSet.noneOf(enumClass) : EnumSet.copyOf(list),
                List::copyOf
        );
    }

    // 2. Create a method that returns the "Group" of parent fields without building the final object.
    // The generic <O> allows this to be used by Subclasses.
    protected static <E extends Enum<E>, O extends EnumSelectorComponent<E>>
    Products.P2<RecordCodecBuilder.Mu<O>, EnumSet<E>, EnumSet<E>> commonFields(
            RecordCodecBuilder.Instance<O> inst,
            Codec<EnumSet<E>> setCodec) {

        return inst.group(
                setCodec.fieldOf("allowed").forGetter(EnumSelectorComponent::getAllowed),
                setCodec.fieldOf("enabled").forGetter(EnumSelectorComponent::getEnabled)
        );
    }

    // Your original method can now use these helpers
    protected static <E extends Enum<E>, T extends EnumSelectorComponent<E>> Codec<T> createCodec(
            Class<E> enumClass,
            Codec<E> enumCodec,
            BiFunction<EnumSet<E>, EnumSet<E>, T> factory) {

        Codec<EnumSet<E>> setCodec = getSetCodec(enumClass, enumCodec);
        return RecordCodecBuilder.create(inst ->
                commonFields(inst, setCodec).apply(inst, factory)
        );
    }
}
