package io.github.daxigua2333.mocai_clues.data.sync;

import com.google.gson.*;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.tuples.Pair;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class NitriteDocumentJson {
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private NitriteDocumentJson() {}

    // Document -> JSON
    public static String toJson(Document doc) {
        return GSON.toJson(toJsonObject(doc));
    }

    // JSON -> Document
    public static Document fromJson(String json) {
        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Root JSON value must be an object");
        }
        return fromJsonObject(root.getAsJsonObject());
    }

    public static JsonObject toJsonObject(Document doc) {
        JsonObject out = new JsonObject();
        for (Pair<String, Object> p : doc) {
            out.add(p.getFirst(), toJsonValue(p.getSecond()));
        }
        return out;
    }

    private static JsonElement toJsonValue(Object v) {
        if (v == null) return JsonNull.INSTANCE;

        if (v instanceof Document d) return toJsonObject(d);
        if (v instanceof String s) return new JsonPrimitive(s);
        if (v instanceof Number n) return new JsonPrimitive(n);
        if (v instanceof Boolean b) return new JsonPrimitive(b);
        if (v instanceof Character c) return new JsonPrimitive(c);

        if (v instanceof Iterable<?> it) {
            JsonArray arr = new JsonArray();
            for (Object x : it) arr.add(toJsonValue(x));
            return arr;
        }

        if (v.getClass().isArray()) {
            JsonArray arr = new JsonArray();
            int len = Array.getLength(v);
            for (int i = 0; i < len; i++) arr.add(toJsonValue(Array.get(v, i)));
            return arr;
        }

        // Fallback: JSON cannot represent arbitrary Java objects; encode as string.
        return new JsonPrimitive(String.valueOf(v));
    }

    public static Document fromJsonObject(JsonObject obj) {
        Document doc = Document.createDocument(); // exists in 4.x
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            doc.put(e.getKey(), fromJsonValue(e.getValue()));
        }
        return doc;
    }

    private static Object fromJsonValue(JsonElement e) {
        if (e == null || e.isJsonNull()) return null;

        if (e.isJsonObject()) return fromJsonObject(e.getAsJsonObject());

        if (e.isJsonArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonElement x : e.getAsJsonArray()) list.add(fromJsonValue(x));
            return list;
        }

        JsonPrimitive p = e.getAsJsonPrimitive();
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isString()) return p.getAsString();

        // Numbers are untyped in JSON; pick a reasonable Java type.
        BigDecimal bd = p.getAsBigDecimal();
        if (bd.scale() <= 0) {
            try { return bd.intValueExact(); } catch (ArithmeticException ignore) {}
            try { return bd.longValueExact(); } catch (ArithmeticException ignore) {}
            return bd.toBigInteger();
        }
        return bd; // preserves precision
    }
}
