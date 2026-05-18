package io.github.daxigua2333.cmagic_clue.decal.common;


/**
 * Represents a stitched or allocated region within the BigAtlasManager.
 */
public record AtlasRegion(int pixelX, int pixelY, int width, int height, int atlasSize) {

    // UV mappings normalized between 0.0f and 1.0f
    public float getU0() {
        return (float) pixelX / atlasSize;
    }

    public float getV0() {
        return (float) pixelY / atlasSize;
    }

    public float getU1() {
        return (float) (pixelX + width) / atlasSize;
    }

    public float getV1() {
        return (float) (pixelY + height) / atlasSize;
    }
}
