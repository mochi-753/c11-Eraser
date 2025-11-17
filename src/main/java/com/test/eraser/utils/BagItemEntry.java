package com.test.eraser.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import static com.mojang.text2speech.Narrator.LOGGER;

public class BagItemEntry {
    private final Item item;
    private final long count;
    private final CompoundTag tag;

    public BagItemEntry(Item item, long count, CompoundTag tag) {
        this.item = item;
        this.count = count;
        this.tag = tag;
    }

    public Item getItem() { return item; }
    public long getCount() { return count; }
    public CompoundTag getTag() { return tag; }

    public static BagItemEntry fromItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return new BagItemEntry(stack.getItem(), 0, null);
        }
        return new BagItemEntry(stack.getItem(), stack.getCount(), stack.getTag());
    }

    // BagItemEntry.java
    public ItemStack toItemStack() {
        // ログ: toItemStack が呼ばれた
        LOGGER.debug("BagItemEntry.toItemStack called. Item: {}, Long Count: {}, Tag: {}", this.item, this.count, this.tag);

        if (this.item == null || this.item == net.minecraft.world.item.Items.AIR) {
            LOGGER.debug("BagItemEntry.item is null or AIR, returning EMPTY.");
            return ItemStack.EMPTY;
        }

        int itemStackCount = (int) Math.min(this.count, Integer.MAX_VALUE);

        // ログ: count が int に変換された値
        LOGGER.debug("BagItemEntry.count (long): {} converted to int count: {}", this.count, itemStackCount);

        if (itemStackCount <= 0) {
            LOGGER.warn("BagItemEntry.count resulted in itemStackCount <= 0 (was: {}). Returning EMPTY.", itemStackCount);
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(this.item, itemStackCount);
        if (this.tag != null) {
            stack.setTag(this.tag.copy());
        }

        // ログ: 最終的に作成された ItemStack の情報
        LOGGER.debug("BagItemEntry.toItemStack returning ItemStack: {} with count: {}", stack.getItem(), stack.getCount());

        return stack;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("id", ForgeRegistries.ITEMS.getKey(this.item).toString());
        nbt.putLong("Count", this.count);
        if (this.tag != null) {
            nbt.put("tag", this.tag.copy());
        }
        return nbt;
    }

    @SuppressWarnings("removal")
    public static BagItemEntry deserializeNBT(CompoundTag nbt) {
        String itemId = nbt.getString("id");
        Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(itemId));
        if (item == null) {
            System.err.println("Unknown item ID: " + itemId);
            return new BagItemEntry(net.minecraft.world.item.Items.AIR, 0, null);
        }
        long count = nbt.getLong("Count"); // getLong を使用
        CompoundTag tag = nbt.contains("tag") ? nbt.getCompound("tag") : null;

        return new BagItemEntry(item, count, tag);
    }
}