package io.github.daxigua2333.cmagic_clue;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class Config {
    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;

    static {
        Pair<Client, ModConfigSpec> clientPair =
                new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
        Pair<Server, ModConfigSpec> serverPair =
                new ModConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();
        Pair<Common, ModConfigSpec> commonPair =
                new ModConfigSpec.Builder().configure(Common::new);
        COMMON = commonPair.getLeft();
        COMMON_SPEC = commonPair.getRight();

    }

    public static class Client{
//        public final ModConfigSpec.BooleanValue FOOTPRINT_DO_RENDER;
//        public final ModConfigSpec.IntValue FOOTPRINT_RENDER_DISTANCE;
//        public final ModConfigSpec.IntValue FOOTPRINT_R;
//        public final ModConfigSpec.IntValue FOOTPRINT_G;
//        public final ModConfigSpec.IntValue FOOTPRINT_B;

        public final ModConfigSpec.IntValue FLASH_DOT_R;
        public final ModConfigSpec.IntValue FLASH_DOT_G;
        public final ModConfigSpec.IntValue FLASH_DOT_B;

        Client(ModConfigSpec.Builder builder) {
//            builder.push("footprint");
//            FOOTPRINT_DO_RENDER = builder
//                    .comment("whether client rendering all the footprints")
//                    .define("footprintDoRender", true);
//            FOOTPRINT_RENDER_DISTANCE = builder
//                    .comment("render distance of footprints")
//                    .defineInRange("footprintRenderDistance", 64, 1, 256);
//            FOOTPRINT_R = builder
//                    .comment("footprint color: R")
//                    .defineInRange("footprintR", 255, 0, 255);
//            FOOTPRINT_G = builder
//                    .comment("footprint color: G")
//                    .defineInRange("footprintG", 215, 0, 255);
//            FOOTPRINT_B = builder
//                    .comment("footprint color: B")
//                    .defineInRange("footprintB", 0, 0, 255);
//            builder.pop();

            builder.push("discovery");
            FLASH_DOT_R = builder
                    .comment("flash dot color: R")
                    .defineInRange("flashDotR", 0, 0, 255);
            FLASH_DOT_G = builder
                    .comment("flash dot color: G")
                    .defineInRange("flashDotG", 0, 0, 255);
            FLASH_DOT_B = builder
                    .comment("flash dot color: B")
                    .defineInRange("flashDotB", 0, 0, 255);
            builder.pop();
        }
    }
    public static class Server{
//        public final ModConfigSpec.DoubleValue FOOTPRINT_EXPIRE_RATE;
//        public final ModConfigSpec.IntValue FOOTPRINT_CREATE_FREQUENCY;
//        public final ModConfigSpec.IntValue FOOTPRINT_LIFETIME;
//        public final ModConfigSpec.DoubleValue FOOTPRINT_INIT_ALPHA;
//        public final ModConfigSpec.DoubleValue FOOTPRINT_TINY_DISTANCE;

        Server(ModConfigSpec.Builder builder) {
//            builder.push("footprint");
//
//            FOOTPRINT_CREATE_FREQUENCY = builder
//                    .comment("Each N ticks generates a footprint")
//                    .defineInRange("footprintCreateFrequency", 10, 1,20);
//            FOOTPRINT_LIFETIME = builder
//                    .comment("lifetime of footprints (ticks)")
//                    .defineInRange("footprintLifetime", 20, 5, 20*3600*2);
//            FOOTPRINT_EXPIRE_RATE = builder
//                    .comment("Footprints' alpha will decrease with this rate in its lifetime")
//                    .defineInRange("footprintExpireRate", 0.1, 0, 1);
//            FOOTPRINT_INIT_ALPHA = builder
//                    .comment("Footprints' alpha will decrease from this initial value in its lifetime")
//                            .defineInRange("footprintInitAlpha", 0.9, 0, 1);
////            FOOTPRINT_TINY_DISTANCE = builder
////                    .comment("The tiny distance when rendering footprints")
////                    .defineInRange("footprintTinyDistance", 0.01, 1e-4, 0.1);
//
//            builder.pop();
        }
    }
    public static class Common{
//        public final ModConfigSpec.BooleanValue WAND_LEFT_CLICK_CANCEL;
//        public final ModConfigSpec.IntValue WAND_HIGHLIGHT_RADIUS;
//        public final ModConfigSpec.DoubleValue FINDER_HIT_DISTANCE;

        Common(ModConfigSpec.Builder builder) {
//            WAND_LEFT_CLICK_CANCEL = builder
//                    .comment("Whether to cancel the normal left click event when switching wand mode")
//                    .define("wandLeftClickCancel", true);
//
//            WAND_HIGHLIGHT_RADIUS = builder
//                    .comment("Holding the wand will highlight all the clues in this range")
//                    .defineInRange("wandHighlightRadius", 64, 1, 256);
//
//            FINDER_HIT_DISTANCE = builder
//                    .comment("Holding the finder and aiming the crosshair at clue block in this distance will get a feedback")
//                    .defineInRange("finderHitDistance", 20.0D, 1.0D, 128.0D);

        }
    }





//    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
//            .comment("What you want the introduction message to be for the magic number")
//            .define("magicNumberIntroduction", "The magic number is... ");
//
//    // a list of strings that are treated as resource locations for items
//    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
//            .comment("A list of items to log on common setup.")
//            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), () -> "", Config::validateItemName);


}
