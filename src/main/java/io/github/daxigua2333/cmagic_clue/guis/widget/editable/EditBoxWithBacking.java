package io.github.daxigua2333.cmagic_clue.guis.widget.editable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * EditBox with a T type backing data
 * Take in getter, setter(for immutables), parser(String -> T), validator(validate T) to achieve this
 * Use static helpers to create one
 */
public class EditBoxWithBacking<T> extends EditBox {
    private enum Type {
        INT, FLOAT, STRING, UUID
    }

    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Function<String, T> parser;
    private final Predicate<T> validator;
    private final Type type;

    private static final int MAX_LENGTH = 512;

    public EditBoxWithBacking(int x, int y, int width, int height,
                              Type type, Supplier<T> getter, Consumer<T> setter, Function<String, T> parser, Predicate<T> validator,
                              Component label) {
        super(Minecraft.getInstance().font, x, y, width, height, label);
        this.type = type;
        this.getter = getter;
        this.setter = setter;
        this.parser = parser;
        this.validator = validator != null ? validator : t -> true;

        setMaxLength(MAX_LENGTH);
        setResponder(this::onTextChanged);

        T initial = getter.get();
        setValue(initial != null ? initial.toString() : "");
        // idk why sometimes it behave strange... so just add this
        setCursorPosition(0);
        setHighlightPos(0);
    }

    public Type getType() {
        return type;
    }


    private void onTextChanged(String text) {
        if (text == null || text.isEmpty()) {
            // empty => mark as invalid and don't update backing value
            setTextColor(0xFF5555);
            return;
        }

        try {
            T parsed = parser.apply(text);
            if (parsed != null && validator.test(parsed)) {
                // only commit when valid
                setter.accept(parsed);
                setTextColor(0xE0E0E0);      // normal text color
            } else {
                setTextColor(0xFF5555);      // invalid
            }
        } catch (RuntimeException ex) {
            // parse failed
            setTextColor(0xFF5555);
        }
    }

    // -------------------------------------------------------------------------
    // Factory helpers: INT / FLOAT / STRING
    // -------------------------------------------------------------------------

    public static EditBoxWithBacking<Integer> intBox(int x, int y, int width, int height,
                                                     Supplier<Integer> getter, Consumer<Integer> setter,
                                                     Predicate<Integer> validator,
                                                     Component label) {
        Function<String, Integer> parser = s -> Integer.parseInt(s.trim());
        return new EditBoxWithBacking<>(x, y, width, height,
                Type.INT, getter, setter, parser, validator, label);
    }

    public static EditBoxWithBacking<Float> floatBox(int x, int y, int width, int height,
                                                     Supplier<Float> getter, Consumer<Float> setter,
                                                     Predicate<Float> validator,
                                                     Component label) {
        Function<String, Float> parser = s -> Float.parseFloat(s.trim());
        return new EditBoxWithBacking<>(x, y, width, height,
                Type.FLOAT, getter, setter, parser, validator, label);
    }

    public static EditBoxWithBacking<String> stringBox(int x, int y, int width, int height,
                                                       Supplier<String> getter, Consumer<String> setter,
                                                       Predicate<String> validator,
                                                       Component label) {
        Function<String, String> parser = s -> s; // no parsing
        return new EditBoxWithBacking<>(x, y, width, height,
                Type.STRING, getter, setter, parser, validator, label);
    }

    public static EditBoxWithBacking<UUID> uuidBox(int x, int y, int width, int height,
                                                   Supplier<UUID> getter, Consumer<UUID> setter,
                                                   Predicate<UUID> validator,
                                                   Component label) {
        Function<String, UUID> parser = UUID::fromString;
        return new EditBoxWithBacking<>(x, y, width, height,
                Type.UUID, getter, setter, parser, validator, label);
    }
}
