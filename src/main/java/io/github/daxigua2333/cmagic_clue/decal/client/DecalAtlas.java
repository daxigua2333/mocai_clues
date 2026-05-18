package io.github.daxigua2333.cmagic_clue.decal.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.daxigua2333.cmagic_clue.CMagicClue;
import io.github.daxigua2333.cmagic_clue.decal.common.AtlasRegion;
import io.github.daxigua2333.cmagic_clue.decal.common.DecalMisc;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A specialized 4096x4096 Texture Atlas divided into 16x16 grids.
 * Handles both static batch-loading on init and dynamic runtime grid allocation/updating.
 */
@OnlyIn(Dist.CLIENT)
public class DecalAtlas extends DynamicTexture {

    private static final int ATLAS_SIZE = DecalMisc.ATLAS_SIZE;
    private static final int GRID_SIZE = DecalMisc.ATLAS_GRID_SIZE;
    private static final int GRID_COUNT = ATLAS_SIZE / GRID_SIZE; // 256
    private static final int TOTAL_GRIDS = GRID_COUNT * GRID_COUNT; // 65,536

    private final BitSet usedGrids = new BitSet(TOTAL_GRIDS);
    private final Map<ResourceLocation, AtlasRegion> staticRegions = new HashMap<>();
    private final ResourceLocation atlasLocation;

    public DecalAtlas(ResourceLocation atlasLocation) {
        // Initialize a 4096x4096 RGBA NativeImage backing
        super(new NativeImage(NativeImage.Format.RGBA, ATLAS_SIZE, ATLAS_SIZE, false));
        this.atlasLocation = atlasLocation;
    }

    /**
     * Export for debug purpose
     */
    public void export() {
        try {
            Path debugPath = FMLPaths.GAMEDIR.get().resolve("debug_atlas.png");
            if (this.getPixels() != null) {
                this.getPixels().writeToFile(debugPath);
            }
            CMagicClue.LOGGER.info("Successfully exported debug atlas to: {}", debugPath.toAbsolutePath());
        } catch (Exception e) {
            CMagicClue.LOGGER.error("Failed to write debug atlas to disk", e);
        }

    }

    /**
     * Registers this atlas to the Minecraft TextureManager.
     */
    public void register(TextureManager textureManager) {
        textureManager.register(this.atlasLocation, this);
    }

    /**
     * Retrieves the ResourceLocation for RenderTypes to use.
     */
    public ResourceLocation getLocation() {
        return this.atlasLocation;
    }

    /**
     * Loads static textures from the ResourceManager.
     * Must be called during texture stitching / reload phase.
     */
    public void initStatic(ResourceManager resourceManager, List<ResourceLocation> staticTextures) {
        RenderSystem.assertOnRenderThread();
        NativeImage backingImage = this.getPixels();
        if (backingImage == null) throw new IllegalStateException("Backing NativeImage is null!");

        for (ResourceLocation location : staticTextures) {
            try {
                Resource resource = resourceManager.getResourceOrThrow(location);
                try (InputStream is = resource.open()) {
                    NativeImage srcImage = NativeImage.read(is);
                    int width = srcImage.getWidth();
                    int height = srcImage.getHeight();

                    if (width % GRID_SIZE != 0 || height % GRID_SIZE != 0) {
                        throw new IllegalArgumentException("Texture " + location + " dimensions are not multiples of 16!");
                    }

                    int gridsX = width / GRID_SIZE;
                    int gridsY = height / GRID_SIZE;

                    // Allocate contiguous space
                    GridPos pos = allocateSpace(gridsX, gridsY);
                    if (pos == null) {  // TODO: handle full
                        throw new RuntimeException("Atlas is full! Cannot fit static texture: " + location);
                    }

                    int pixelX = pos.x * GRID_SIZE;
                    int pixelY = pos.y * GRID_SIZE;

                    // Copy pixels into backing image
                    copyImageRegion(srcImage, backingImage, 0, 0, pixelX, pixelY, width, height);

                    // Store UV region
                    staticRegions.put(location, new AtlasRegion(pixelX, pixelY, width, height, ATLAS_SIZE));
                    srcImage.close();
                }
            } catch (IOException e) {
                CMagicClue.LOGGER.error("", e);
            }
        }
//        export();
        // Upload the entire static stitched image to VRAM once
        this.upload();
    }

    public void freeStatic() {
        for (AtlasRegion region : staticRegions.values()) {
            freeDynamic(region);
        }
        staticRegions.clear();
    }

    /**
     * Dynamically allocates a 16x16 grid at runtime.
     */
    @Nullable
    public AtlasRegion allocateDynamic() {
        GridPos pos = allocateSpace(1, 1);
        if (pos == null) return null; // TODO: handle full
        return new AtlasRegion(pos.x * GRID_SIZE, pos.y * GRID_SIZE, GRID_SIZE, GRID_SIZE, ATLAS_SIZE);
    }

    /**
     * Updates a dynamically allocated region with a new NativeImage.
     * MUST be called on the Render Thread! Uses partial glTexSubImage2D uploading.
     */
    public void updateDynamic(AtlasRegion region, NativeImage newImage) {
        RenderSystem.assertOnRenderThread();

        if (newImage.getWidth() != GRID_SIZE || newImage.getHeight() != GRID_SIZE) {
            throw new IllegalArgumentException("Dynamic updates must be 16x16 images!");
        }

        NativeImage backingImage = this.getPixels();
        if (backingImage == null) return;

        // 1. Copy to backing RAM image (keeps RAM and VRAM in sync in case of context loss)
        copyImageRegion(newImage, backingImage, 0, 0, region.pixelX(), region.pixelY(), GRID_SIZE, GRID_SIZE);

        // 2. Bind the Atlas Texture
        RenderSystem.bindTexture(this.getId());

        // 3. Partial Upload to VRAM (glTexSubImage2D mapping)
        // unpackSkipPixels/Rows dictates where to start reading from the BACKING image
        // xOffset/yOffset dictates where to write to the VRAM texture
        backingImage.upload(
                0, // mipmap level
                region.pixelX(), region.pixelY(), // VRAM xOffset, yOffset
                region.pixelX(), region.pixelY(), // RAM unpackSkipPixels, unpackSkipRows
                GRID_SIZE, GRID_SIZE, // Width, Height to upload
                false, false, false, false // blur, clamp, mipmap, autoClose
        );
    }

    /**
     * Updates a dynamically allocated region using raw RGBA pixel data.
     * MUST be called on the Render Thread! Uses partial glTexSubImage2D uploading.
     *
     * @param region     The allocated atlas region.
     * @param rgbaPixels Compact byte array of size 16x16x4 = 1024 bytes (RGBA format).
     */
    public void updateDynamic(AtlasRegion region, byte[] rgbaPixels) {
        RenderSystem.assertOnRenderThread();
        // 16 pixels * 16 pixels * 4 bytes (R, G, B, A) = 1024 bytes
        if (rgbaPixels.length != GRID_SIZE * GRID_SIZE * 4) {
            throw new IllegalArgumentException("Dynamic updates must receive exactly 1024 bytes (16x16 RGBA)!");
        }
        NativeImage backingImage = this.getPixels();
        if (backingImage == null) return;

        copyImageRegion(rgbaPixels, backingImage, region.pixelX(), region.pixelY(), GRID_SIZE, GRID_SIZE);

        RenderSystem.bindTexture(this.getId());
        backingImage.upload(
                0,
                region.pixelX(), region.pixelY(),
                region.pixelX(), region.pixelY(),
                GRID_SIZE, GRID_SIZE,
                false, false, false, false
        );
//        export();
    }


    /**
     * Frees a dynamically allocated region so it can be reused.
     */
    public void freeDynamic(AtlasRegion region) {
        int startGridX = region.pixelX() / GRID_SIZE;
        int startGridY = region.pixelY() / GRID_SIZE;
        int gridsW = region.width() / GRID_SIZE;
        int gridsH = region.height() / GRID_SIZE;

        for (int y = 0; y < gridsH; y++) {
            int startIndex = (startGridY + y) * GRID_COUNT + startGridX;
            usedGrids.clear(startIndex, startIndex + gridsW);
        }
    }

    @Nullable
    public AtlasRegion getStaticRegion(ResourceLocation location) {
        return staticRegions.get(location);
    }

    // --- INTERNAL ARCHITECTURE / ALLOCATION LOGIC ---

    /**
     * 2D Shelf/Grid Allocation logic.
     * Finds the first available contiguous block of gridsX * gridsY.
     * ATTENTION: expensive!!
     */
    @Nullable
    private GridPos allocateSpace(int gridsW, int gridsH) {
        // 1*1 optimization
        if (gridsW == 1 && gridsH == 1) {
            int index = usedGrids.nextClearBit(0);
            if (index >= usedGrids.length()) return null;
            usedGrids.set(index);
            return new GridPos(index % GRID_COUNT, index / GRID_COUNT);
        }

        for (int y = 0; y <= GRID_COUNT - gridsH; y++) {
            for (int x = 0; x <= GRID_COUNT - gridsW; x++) {
                if (checkSpaceFree(x, y, gridsW, gridsH)) {
                    markSpaceUsed(x, y, gridsW, gridsH);
                    return new GridPos(x, y);
                }
            }
        }
        return null;
    }

    private boolean checkSpaceFree(int startX, int startY, int width, int height) {
        for (int y = 0; y < height; y++) {
            int startIndex = (startY + y) * GRID_COUNT + startX;
            int nextSet = usedGrids.nextSetBit(startIndex);
            if (nextSet != -1 && nextSet < startIndex + width) {
                return false;
            }
        }
        return true;
    }

    private void markSpaceUsed(int startX, int startY, int width, int height) {
        for (int y = 0; y < height; y++) {
            int startIndex = (startY + y) * GRID_COUNT + startX;
            usedGrids.set(startIndex, startIndex + width);
        }
    }

    /**
     * Utility to copy pixels between NativeImages securely.
     */
    private void copyImageRegion(NativeImage src, NativeImage dest, int srcX, int srcY, int destX, int destY, int width, int height) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                dest.setPixelRGBA(destX + x, destY + y, src.getPixelRGBA(srcX + x, srcY + y));
            }
        }
    }

    /**
     * Utility to copy raw RGBA bytes into a NativeImage directly.
     * Avoids NativeImage instantiation overhead for the source.
     */
    private void copyImageRegion(byte[] srcPixels, NativeImage dest, int destX, int destY, int width, int height) {
        int i = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {  // it is said that there s no need to copy row by row (must use reflection or mixin), because compiler will optimize the double for loop
                // Read 4 bytes from the array
                byte r = srcPixels[i++];
                byte g = srcPixels[i++];
                byte b = srcPixels[i++];
                byte a = srcPixels[i++];
                // NativeImage RGBA format internally represents colors as ABGR integers.
                // This is because of Little Endian memory layouts (R is the lowest byte).
                int color = ((a & 0xFF) << 24) |
                        ((b & 0xFF) << 16) |
                        ((g & 0xFF) << 8) |
                        (r & 0xFF);
                // Write the packed 32-bit int directly
                dest.setPixelRGBA(destX + x, destY + y, color);
            }
        }
    }

    private record GridPos(int x, int y) {
    }
}
