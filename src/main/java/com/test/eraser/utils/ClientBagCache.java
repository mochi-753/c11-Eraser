package com.test.eraser.utils;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClientBagCache {
    private static final Map<UUID, CachedBagData> cache = new HashMap<>();

    public static void put(UUID bagId, int page,
                           List<ItemStack> prev, List<ItemStack> current, List<ItemStack> next) {
        cache.put(bagId, new CachedBagData(page, prev, current, next));
    }

    public static CachedBagData get(UUID bagId) {
        return cache.get(bagId);
    }

    public static void clear(UUID bagId) {
        cache.remove(bagId);
    }

    public record CachedBagData(int page,
                                List<ItemStack> prev,
                                List<ItemStack> current,
                                List<ItemStack> next) {}
}
