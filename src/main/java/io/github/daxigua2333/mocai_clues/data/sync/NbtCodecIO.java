package io.github.daxigua2333.mocai_clues.data.sync;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;

public final class NbtCodecIO {
    private NbtCodecIO() {}

    public static <T> byte[] encodeToBytes(Codec<T> codec, T value) {
        Objects.requireNonNull(codec);
        Objects.requireNonNull(value);

        Tag tag = codec.encodeStart(NbtOps.INSTANCE, value)
                .getOrThrow(msg -> new IllegalStateException("Encode failed: " + msg));

        CompoundTag root = new CompoundTag();
        root.put("v", tag);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.writeCompressed(root, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("NBT encode (compressed) failed", e);
        }
    }

    public static <T> T decodeFromBytes(Codec<T> codec, byte[] bytes) {
        Objects.requireNonNull(codec);
        Objects.requireNonNull(bytes);

        final CompoundTag root;
        try {
            root = NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
        } catch (Exception e) {
            throw new IllegalStateException("NBT decode (compressed) failed", e);
        }

        Tag tag = root.get("v");
        if (tag == null) {
            throw new IllegalStateException("Missing NBT root key 'v'");
        }

        DataResult<T> res = codec.parse(NbtOps.INSTANCE, tag);
        return res.getOrThrow(msg -> new IllegalStateException("Decode failed: " + msg));
    }
}
