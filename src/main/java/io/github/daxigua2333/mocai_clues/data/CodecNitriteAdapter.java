package io.github.daxigua2333.mocai_clues.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.dizitart.no2.collection.Document;

public final class CodecNitriteAdapter<T> {
    private final Codec<T> codec;
    private final Gson gson = new Gson();

    public CodecNitriteAdapter(Codec<T> codec) {
        this.codec = codec;
    }

    public Document toDocument(T value) {
        DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, value);
        JsonElement json = result.getOrThrow(msg -> {
            throw new IllegalStateException("Codec encode failed: " + msg);
        });
        String jsonString = gson.toJson(json);
        return Document.createDocument("data", jsonString);
    }

    public T fromDocument(Document doc) {
        String jsonString = doc.get("data", String.class);
        JsonElement json = JsonParser.parseString(jsonString);
        DataResult<T> result = codec.parse(JsonOps.INSTANCE, json);
        return result.getOrThrow(msg -> {
            throw new IllegalStateException("Codec decode failed: " + msg);
        });
    }
}
