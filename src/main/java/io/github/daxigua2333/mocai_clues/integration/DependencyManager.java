package io.github.daxigua2333.mocai_clues.integration;

import net.neoforged.fml.ModList;

public final class DependencyManager {
    public enum Mod {
        C_MAGIC("cmagic"),
        ;

        private final String id;

        Mod(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

//    public static final EnumSet<ModId> loaded;
    public static boolean isLoaded(Mod mod) {
        return ModList.get().isLoaded(mod.getId());
    }
}
