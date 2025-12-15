package io.github.daxigua2333.mocai_clues.data.sync.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.dizitart.no2.collection.Document;

import java.util.List;
import java.util.Objects;

/**
 * Codec <-> Nitrite {@link Document} bridge.
 *
 * <p>This implementation uses {@link JsonOps} (Gson {@link JsonElement}) as the intermediate format, then
 * converts that JSON tree into a Nitrite {@link Document}/{@link List} of primitives. This keeps individual
 * fields addressable so that Nitrite indexes can be created on them.
 *
 * <p>Important: this class intentionally does NOT persist values as opaque {@code byte[]} blobs.
 */
public final class DocumentCodecIO {
    private DocumentCodecIO() {
    }

    public static <T> Document encodeToDocument(Codec<T> codec, T value) {
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(value, "value");

        JsonElement json = codec.encodeStart(JsonOps.INSTANCE, value)
                .getOrThrow(msg -> new IllegalStateException("Encode failed: " + msg));

        return DocumentJsonIO.fromJsonObject(json.getAsJsonObject());

    }

    public static <T> T decodeFromDocument(Codec<T> codec, Document doc) {
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(doc, "doc");

        JsonObject json = DocumentJsonIO.toJsonObject(doc);
        DataResult<T> res = codec.parse(JsonOps.INSTANCE, json == null ? JsonNull.INSTANCE : json);

        return res.getOrThrow(msg -> new IllegalStateException("Decode failed: " + msg));
    }
}
