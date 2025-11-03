package com.test.eraser.utils;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class Res {
    public static ResourceLocation getResource(String modid, String path) {
        return new ResourceLocation(modid, path);
    }
}