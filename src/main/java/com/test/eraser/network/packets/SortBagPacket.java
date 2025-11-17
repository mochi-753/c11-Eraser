package com.test.eraser.network.packets;

import com.test.eraser.gui.BagMenu;
import com.test.eraser.network.PacketHandler;
import com.test.eraser.utils.BagItemEntry;
import com.test.eraser.utils.BagSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Supplier;

public class SortBagPacket {
    public enum SortType {
        NAME,
        COUNT,
    }

    private final UUID bagId;
    private final SortType sortType;

    public SortBagPacket(UUID bagId, SortType sortType) {
        this.bagId = bagId;
        this.sortType = sortType;
    }

    public static void encode(SortBagPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.bagId);
        buf.writeEnum(pkt.sortType);
    }

    public static SortBagPacket decode(FriendlyByteBuf buf) {
        return new SortBagPacket(buf.readUUID(), buf.readEnum(SortType.class));
    }

    public static void handle(SortBagPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BagSavedData data = BagSavedData.get(player.serverLevel());

                List<BagItemEntry> allEntries = data.getBag(pkt.bagId);
                List<BagItemEntry> oldAllEntries = new ArrayList<>(allEntries);

                List<BagItemEntry> nonEmptyEntries = new ArrayList<>();
                for (BagItemEntry entry : allEntries) {
                    if (entry.getCount() > 0 && entry.getItem() != net.minecraft.world.item.Items.AIR) {
                        nonEmptyEntries.add(entry);
                    }
                }

                Map<BagItemEntry, BagItemEntry> consolidatedMap = new HashMap<>();
                for (BagItemEntry entryToCheck : nonEmptyEntries) {
                    BagItemEntry existingEntry = null;
                    for (Map.Entry<BagItemEntry, BagItemEntry> mapEntry : consolidatedMap.entrySet()) {
                        if (areEntriesStackable(mapEntry.getKey(), entryToCheck)) {
                            existingEntry = mapEntry.getValue();
                            break;
                        }
                    }

                    if (existingEntry != null) {
                        long newCount = existingEntry.getCount() + entryToCheck.getCount();
                        consolidatedMap.remove(existingEntry);
                        BagItemEntry newEntry = new BagItemEntry(existingEntry.getItem(), newCount, existingEntry.getTag());
                        consolidatedMap.put(newEntry, newEntry);
                    } else {
                        consolidatedMap.put(entryToCheck, entryToCheck);
                    }
                }
                List<BagItemEntry> finalConsolidated = new ArrayList<>(consolidatedMap.values());

                switch (pkt.sortType) {
                    case NAME:
                        finalConsolidated.sort(Comparator.comparing(entry -> entry.getItem().getDescription().getString().toLowerCase()));
                        break;
                    case COUNT:
                        finalConsolidated.sort((a, b) -> Long.compare(b.getCount(), a.getCount())); // count は long
                        break;
                    default:
                        return;
                }

                allEntries.clear();
                allEntries.addAll(finalConsolidated);

                data.setDirty();

                Set<Integer> affectedPages = new HashSet<>();
                int pageSize = BagSavedData.PAGE_SIZE;

                int maxIndex = Math.max(oldAllEntries.size(), allEntries.size());

                for (int i = 0; i < maxIndex; i++) {
                    BagItemEntry oldEntry = i < oldAllEntries.size() ? oldAllEntries.get(i) : new BagItemEntry(net.minecraft.world.item.Items.AIR, 0, null);
                    BagItemEntry newEntry = i < allEntries.size() ? allEntries.get(i) : new BagItemEntry(net.minecraft.world.item.Items.AIR, 0, null);

                    if (!Objects.equals(oldEntry.getItem(), newEntry.getItem()) ||
                            oldEntry.getCount() != newEntry.getCount() ||
                            !Objects.equals(oldEntry.getTag(), newEntry.getTag())) {
                        int pageIndex = i / pageSize;
                        affectedPages.add(pageIndex);
                    }
                }

                for (int pageNum : affectedPages) {
                    List<ItemStack> prev = (pageNum > 0) ? data.getPage(pkt.bagId, pageNum - 1) : Collections.emptyList();
                    List<ItemStack> current = data.getPage(pkt.bagId, pageNum);
                    List<ItemStack> next = (pageNum < data.getTotalPages(pkt.bagId) - 1) ? data.getPage(pkt.bagId, pageNum + 1) : Collections.emptyList();

                    PacketHandler.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new SyncBagPagesPacket(pkt.bagId, pageNum, prev, current, next)
                    );
                }

            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static boolean areEntriesStackable(BagItemEntry a, BagItemEntry b) {
        return a.getItem() == b.getItem() && Objects.equals(a.getTag(), b.getTag());
    }
}