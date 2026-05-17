package io.github.daxigua2333.cmagic_clue.decal;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public record AtlasRLWithUV(ResourceLocation atlasResourceLocation, @Nullable AtlasRegion atlasRegion) {
}
