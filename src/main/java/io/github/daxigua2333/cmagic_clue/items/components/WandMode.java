package io.github.daxigua2333.cmagic_clue.items.components;

public enum WandMode {
//    CREATE,
//    DELETE,
//    QUERY;
//    CREATE_INFINITY,
    EDITOR,
//    ATTACH,
    ;
    public WandMode next() {
        WandMode[] vals = values();
        return vals[(this.ordinal() + 1) % vals.length];
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
