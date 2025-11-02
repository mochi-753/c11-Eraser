package com.test.eraser.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public enum DestroyMode {
    NORMAL(1, 1, 1, 0),
    SMALL(1, 2, 2, 0),
    CUBE(3, 3, 3, 0),
    TUNNEL(3, 3, 7, 0);

    private static final String KEY = "WorldDestroyerMode";
    private static final String SILK_KEY = "SilkTouchEnabled"; // ← 追加
    public final int x, y, z;
    public final int yOffset;
    DestroyMode(int x, int y, int z, int yOffset) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yOffset = yOffset;
    }

    public static DestroyMode cycleMode(ItemStack stack) {
        DestroyMode current = getMode(stack);
        DestroyMode next = values()[(current.ordinal() + 1) % values().length];
        setMode(stack, next);
        return next;
    }

    public static DestroyMode getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains(KEY)) {
            int ordinal = tag.getInt(KEY);
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
        }
        return NORMAL;
    }

    public static void setMode(ItemStack stack, DestroyMode mode) {
        stack.getOrCreateTag().putInt(KEY, mode.ordinal());
    }

    public static boolean isSilkTouchEnabled(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean(SILK_KEY);
    }

    public static void setSilkTouch(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(SILK_KEY, enabled);
    }

    public static boolean toggleSilkTouch(ItemStack stack) {
        boolean current = isSilkTouchEnabled(stack);
        boolean next = !current;
        setSilkTouch(stack, next);
        return next;
    }
}
