package io.github.daxigua2333.mocai_clues.guis.widget.editable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EditBoxRow<T> extends EditBox {
    private enum Type {
        INT, FLOAT, STRING
    }

    private final Supplier<T> getter;
    private final Consumer<T> setter;
    private final Function<String, T> parser;
    private final Predicate<T> validator;
    private final Type type;

    private final int vPadding;
    private final int hPadding;
    private final Font font;


    public EditBoxRow(int x, int y, int width, int height, int vPadding, int hPadding,
                      Type type, Supplier<T> getter, Consumer<T> setter, Function<String, T> parser, Predicate<T> validator,
                      Component label) {
        super(Minecraft.getInstance().font, x, y, width, height, label);
        this.type = type;
        this.getter = getter;
        this.setter = setter;
        this.parser = parser;
        this.validator = validator != null ? validator : t -> true;

        this.vPadding = vPadding;
        this.hPadding = hPadding;
        this.font = Minecraft.getInstance().font;

        T initial = getter.get();
        setValue(initial != null ? initial.toString() : "");

        // react to every change of the text
        setResponder(this::onTextChanged);
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

    public static EditBoxRow<Integer> intBox(int x, int y, int width, int height, int vPadding, int hPadding,
                                               Supplier<Integer> getter, Consumer<Integer> setter,
                                               Predicate<Integer> validator,
                                               Component label) {
        Function<String, Integer> parser = s -> Integer.parseInt(s.trim());
        return new EditBoxRow<>(x, y, width, height, vPadding, hPadding,
                Type.INT, getter, setter, parser, validator, label);
    }

    public static EditBoxRow<Float> floatBox(int x, int y, int width, int height, int vPadding, int hPadding,
                                               Supplier<Float> getter, Consumer<Float> setter,
                                               Predicate<Float> validator,
                                               Component label) {
        Function<String, Float> parser = s -> Float.parseFloat(s.trim());
        return new EditBoxRow<>(x, y, width, height, vPadding, hPadding,
                Type.FLOAT, getter, setter, parser, validator, label);
    }

    public static EditBoxRow<String> stringBox(int x, int y, int width, int height, int vPadding, int hPadding,
                                                 Supplier<String> getter, Consumer<String> setter,
                                                 Predicate<String> validator,
                                                 Component label) {
        Function<String, String> parser = s -> s; // no parsing
        return new EditBoxRow<>(x, y, width, height, vPadding, hPadding,
                Type.STRING, getter, setter, parser, validator, label);
    }
}
