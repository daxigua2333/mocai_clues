package io.github.daxigua2333.mocai_clues.footprints.deprecated;

import net.minecraft.util.Mth;

public final class RectOverlay {
    // Center in world space
    public final float x;
    public final float y;
    public final float z;

    // Long/short half widths
    public final float halfLong;
    public final float halfShort;

    // Precomputed yaw basis
    public final float cosYaw;
    public final float sinYaw;

    // 0..1
    public final float alpha;

    public RectOverlay(double x, double y, double z,
                       float yawDegrees,
                       float longSide,
                       float shortSide,
                       float alpha) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
        this.halfLong = longSide * 0.5f;
        this.halfShort = shortSide * 0.5f;
        float yawRad = (float) Math.toRadians(yawDegrees);
        this.cosYaw = Mth.cos(yawRad);
        this.sinYaw = Mth.sin(yawRad);
        this.alpha = alpha;
    }
}
