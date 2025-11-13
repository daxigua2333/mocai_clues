package io.github.daxigua2333.mocai_clues;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue WAND_LEFT_CLICK_CANCEL = BUILDER
            .comment("Whether to cancel the normal left click event when switching wand mode")
            .define("wandLeftClickCancel", true);

    public static final ModConfigSpec.IntValue WAND_HIGHLIGHT_RADIUS = BUILDER
            .comment("Holding the wand will highlight all the clues in this range")
            .defineInRange("wandHighlightRadius", 64, 1, 256);

    public static final ModConfigSpec.DoubleValue FINDER_HIT_DISTANCE = BUILDER
            .comment("Holding the finder and aiming the crosshair at clue block in this distance will get a feedback")
            .defineInRange("finderHitDistance", 20.0D, 1.0D, 128.0D);


//    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
//            .comment("What you want the introduction message to be for the magic number")
//            .define("magicNumberIntroduction", "The magic number is... ");
//
//    // a list of strings that are treated as resource locations for items
//    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
//            .comment("A list of items to log on common setup.")
//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);

    static final ModConfigSpec SPEC = BUILDER.build();

//    private static boolean validateItemName(final Object obj) {
//        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
//    }
}
