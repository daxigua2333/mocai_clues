package io.github.daxigua2333.mocai_clues.items.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.daxigua2333.mocai_clues.MoCaiClues;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponentsRegistry {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MoCaiClues.MODID);

    // Codec that encodes WandMode as a string
    public static final Codec<WandMode> WAND_MODE_CODEC =
        Codec.STRING.xmap(WandMode::valueOf, WandMode::name);
    // A StreamCodec for network syncing, created from the codec
    public static final StreamCodec<ByteBuf, WandMode> WAND_MODE_STREAM =
        ByteBufCodecs.fromCodec(WAND_MODE_CODEC);
    public static final Supplier<DataComponentType<WandMode>> WAND_MODE =
        DATA_COMPONENTS.registerComponentType("wand_mode", builder -> builder
            .persistent(WAND_MODE_CODEC)                 // optional persistent storage
            .networkSynchronized(WAND_MODE_STREAM)      // sync to client
        );

    public static final Codec<FinderHitResult> FINDER_HIT_RESULT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("prev").forGetter(FinderHitResult::prev),
                    Codec.BOOL.fieldOf("current").forGetter(FinderHitResult::current)
            ).apply(instance, FinderHitResult::new));
    public static final StreamCodec<ByteBuf, FinderHitResult> FINDER_HIT_RESULT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FinderHitResult::prev,
            ByteBufCodecs.BOOL, FinderHitResult::current,
            FinderHitResult::new
    );
    public static final Supplier<DataComponentType<FinderHitResult>> FINDER_HIT_RESULT = DATA_COMPONENTS.registerComponentType(
            "finder_hit_result", builder -> builder
                    .persistent(FINDER_HIT_RESULT_CODEC)
                    .networkSynchronized(FINDER_HIT_RESULT_STREAM_CODEC)
    );


    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
