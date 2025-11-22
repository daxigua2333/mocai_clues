package io.github.daxigua2333.mocai_clues.items.components;

public enum WandMode {
    CREATE,
    DELETE,
//    QUERY;  // TODO
    CREATE_INFINITY;
    public WandMode next() {
        WandMode[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
