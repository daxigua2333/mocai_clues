package io.github.daxigua2333.mocai_clues.footprints.statics;

public final class HookToggle {
    private static boolean ENABLED = false;

    private HookToggle() {
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static boolean toggle() {
        ENABLED = !ENABLED;
        return ENABLED;
    }

    public static void set(boolean value) {
        ENABLED = value;
    }
}
