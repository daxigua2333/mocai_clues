package io.github.daxigua2333.cmagic_clue.decal.common;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;

public record BlockFace(BlockPos pos, Direction face) {
    public static final Codec<BlockFace> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockPos.CODEC.fieldOf("pos").forGetter(BlockFace::pos),
                    Direction.CODEC.fieldOf("face").forGetter(BlockFace::face)
            ).apply(instance, BlockFace::new)
    );

    public static final StreamCodec<ByteBuf, BlockFace> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);


        public Vector3f[] getFaceVertices(float offset) {
        // We use the Direction's step (normal vector) to dynamically apply the offset.
        // E.g. UP has a stepY of 1, so it adds to Y. DOWN has a stepY of -1, so it subtracts.
        float x = pos.getX() + face.getStepX() * offset;
        float y = pos.getY() + face.getStepY() * offset;
        float z = pos.getZ() + face.getStepZ() * offset;
        return switch (face) {
            case DOWN -> new Vector3f[]{
                    new Vector3f(x, y, z),
                    new Vector3f(x + 1, y, z),
                    new Vector3f(x + 1, y, z + 1),
                    new Vector3f(x, y, z + 1)
            };
            case UP -> new Vector3f[]{
                    new Vector3f(x, y + 1, z),
                    new Vector3f(x, y + 1, z + 1),
                    new Vector3f(x + 1, y + 1, z + 1),
                    new Vector3f(x + 1, y + 1, z)
            };
            case NORTH -> new Vector3f[]{
                    new Vector3f(x + 1, y, z),
                    new Vector3f(x, y, z),
                    new Vector3f(x, y + 1, z),
                    new Vector3f(x + 1, y + 1, z)
            };
            case SOUTH -> new Vector3f[]{
                    new Vector3f(x, y, z + 1),
                    new Vector3f(x + 1, y, z + 1),
                    new Vector3f(x + 1, y + 1, z + 1),
                    new Vector3f(x, y + 1, z + 1)
            };
            case WEST -> new Vector3f[]{
                    new Vector3f(x, y, z + 1),
                    new Vector3f(x, y, z),
                    new Vector3f(x, y + 1, z),
                    new Vector3f(x, y + 1, z + 1)
            };
            case EAST -> new Vector3f[]{
                    new Vector3f(x + 1, y, z),
                    new Vector3f(x + 1, y, z + 1),
                    new Vector3f(x + 1, y + 1, z + 1),
                    new Vector3f(x + 1, y + 1, z)
            };
        };
    }
}
