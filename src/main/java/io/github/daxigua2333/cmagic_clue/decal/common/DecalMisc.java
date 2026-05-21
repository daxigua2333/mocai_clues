package io.github.daxigua2333.cmagic_clue.decal.common;

public final class DecalMisc {
    public static final int ATLAS_SIZE = 4096;
    public static final int ATLAS_GRID_SIZE = 16;

    public static int BYTE_PER_PIXEL = 4;  // RGBA
    public static int GRID_BYTE_ARRAY_LENGTH = BYTE_PER_PIXEL * ATLAS_GRID_SIZE * ATLAS_GRID_SIZE;

    // To make the layer slightly outward away from the block, preventing z-fighting
    public static final float LAYER_FLOAT_OFFSET = 0.01f;


}
