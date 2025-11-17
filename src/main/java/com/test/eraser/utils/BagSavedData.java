package com.test.eraser.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class BagSavedData extends SavedData {
    public static final int PAGE_SIZE = 54;

    private final Map<UUID, List<BagItemEntry>> bags = new HashMap<>();

    public static BagSavedData get(Level level) {
        if (level.isClientSide) {
            throw new IllegalStateException("BagSavedData.get() should only be called on server side!");
        }
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(BagSavedData::load, BagSavedData::new, "bag_data");
    }

    public BagSavedData() {}

    public int getTotalPages(UUID bagId) {
        List<BagItemEntry> entries = bags.getOrDefault(bagId, Collections.emptyList());
        int pages = (int)Math.ceil((double)entries.size() / PAGE_SIZE);
        return Math.max(pages, 1);
    }

    public static BagSavedData load(CompoundTag tag) {
        BagSavedData data = new BagSavedData();
        ListTag list = tag.getList("bags", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag bagTag = (CompoundTag) t;
            UUID id = bagTag.getUUID("id");
            ListTag entryList = bagTag.getList("entries", Tag.TAG_COMPOUND);
            List<BagItemEntry> entries = new ArrayList<>();
            for (Tag entryTag : entryList) {
                entries.add(BagItemEntry.deserializeNBT((CompoundTag) entryTag));
            }
            data.bags.put(id, entries);
        }
        return data;
    }

    public void setPage(UUID bagId, int page, List<ItemStack> items) {
        List<BagItemEntry> bagList = getBag(bagId);
        int fromIndex = page * PAGE_SIZE;
        int toIndex = fromIndex + PAGE_SIZE;

        while (bagList.size() < toIndex) {
            bagList.add(new BagItemEntry(net.minecraft.world.item.Items.AIR, 0, null));
        }

        for (int i = 0; i < items.size() && (fromIndex + i) < bagList.size(); i++) {
            ItemStack stack = items.get(i);
            BagItemEntry entry = BagItemEntry.fromItemStack(stack);
            bagList.set(fromIndex + i, entry);
        }

        setDirty();
    }

    public List<ItemStack> getPage(UUID bagId, int page) {
        List<BagItemEntry> allEntries = bags.getOrDefault(bagId, Collections.emptyList());

        int fromIndex = page * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, allEntries.size());

        List<ItemStack> itemStacks = new ArrayList<>();
        for (int i = fromIndex; i < fromIndex + PAGE_SIZE; i++) {
            if (i < allEntries.size()) {
                BagItemEntry entry = allEntries.get(i);
                if (entry.getCount() > 0 && entry.getItem() != net.minecraft.world.item.Items.AIR) {
                    itemStacks.add(entry.toItemStack());
                } else {
                    itemStacks.add(ItemStack.EMPTY);
                }
            } else {
                itemStacks.add(ItemStack.EMPTY);
            }
        }
        return itemStacks;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, List<BagItemEntry>> entry : bags.entrySet()) {
            CompoundTag bagTag = new CompoundTag();
            bagTag.putUUID("id", entry.getKey());

            ListTag entryList = new ListTag();
            for (BagItemEntry bagEntry : entry.getValue()) {
                entryList.add(bagEntry.serializeNBT());
            }
            bagTag.put("entries", entryList);

            list.add(bagTag);
        }
        tag.put("bags", list);
        return tag;
    }

    public void registerBag(UUID id) {
        bags.putIfAbsent(id, new ArrayList<>());
        setDirty();
    }

    public List<BagItemEntry> getBag(UUID id) {
        return bags.computeIfAbsent(id, k -> new ArrayList<>());
    }
}