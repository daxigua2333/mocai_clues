package io.github.daxigua2333.mocai_clues.data.sync.misc;

public enum MyObjectOpType {
    UPSERT((byte) 1),
    DELETE((byte) 2);

    public final byte id;

    MyObjectOpType(byte id) {
        this.id = id;
    }

    public static MyObjectOpType fromId(byte id) {
        return switch (id) {
            case 1 -> UPSERT;
            case 2 -> DELETE;
            default -> throw new IllegalArgumentException("Unknown op id: " + id);
        };
    }
}
