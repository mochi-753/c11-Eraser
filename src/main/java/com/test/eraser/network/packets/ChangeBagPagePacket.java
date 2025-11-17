package com.test.eraser.network.packets;

import com.test.eraser.gui.BagMenu;
import com.test.eraser.network.PacketHandler;
import com.test.eraser.utils.BagSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ChangeBagPagePacket {
    private final UUID bagId;
    private final int newPage;

    public ChangeBagPagePacket(UUID bagId, int newPage) {
        this.bagId = bagId;
        this.newPage = newPage;
    }

    public static void encode(ChangeBagPagePacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.bagId);
        buf.writeInt(pkt.newPage);
    }

    public static ChangeBagPagePacket decode(FriendlyByteBuf buf) {
        return new ChangeBagPagePacket(buf.readUUID(), buf.readInt());
    }

    public static void handle(ChangeBagPagePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                AbstractContainerMenu openContainer = player.containerMenu;
                if (openContainer instanceof BagMenu bagMenu) {
                    if (bagMenu.getBagId().equals(pkt.bagId)) {
                        bagMenu.setPage(pkt.newPage); // setPage で customItemHandler を更新 (後述)
                    } else {
                        System.err.println("Player " + player.getName().getString() + " tried to change page for bag " + pkt.bagId + " but open menu is for " + bagMenu.getBagId());
                    }
                } else {
                    System.err.println("Player " + player.getName().getString() + " sent ChangeBagPagePacket but doesn't have BagMenu open.");
                }

                BagSavedData data = BagSavedData.get(player.serverLevel());
                int totalPages = data.getTotalPages(pkt.bagId);
                int prevPageNum = (pkt.newPage > 0) ? pkt.newPage - 1 : -1;
                int nextPageNum = (pkt.newPage < totalPages - 1) ? pkt.newPage + 1 : -1;

                List<ItemStack> prev = (prevPageNum >= 0) ? data.getPage(pkt.bagId, prevPageNum) : Collections.emptyList();
                List<ItemStack> current = data.getPage(pkt.bagId, pkt.newPage);
                List<ItemStack> next = (nextPageNum >= 0) ? data.getPage(pkt.bagId, nextPageNum) : Collections.emptyList();

                PacketHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncBagPagesPacket(pkt.bagId, pkt.newPage, prev, current, next)
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}