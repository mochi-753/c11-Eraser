package com.test.eraser.gui;

import com.test.eraser.additional.ModMenus;
import com.test.eraser.network.PacketHandler;
import com.test.eraser.network.packets.SyncBagPagesPacket;
import com.test.eraser.utils.BagSavedData;
import com.test.eraser.utils.CustomItemStackHandler;
import com.test.eraser.utils.CustomSlotItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BagMenu extends AbstractContainerMenu {
    private final UUID bagId;
    private int page;
    private final int totalPages;
    public final CustomItemStackHandler customItemHandler;

    public BagMenu(int windowId, Inventory playerInv, UUID bagId, int page, int totalPages) {
        super(ModMenus.BAG_MENU.get(), windowId);
        this.bagId = bagId;
        this.page = page;
        this.totalPages = totalPages;
        this.customItemHandler = new CustomItemStackHandler(54);

        layoutSlots(playerInv);

        BagSavedData data = BagSavedData.get(playerInv.player.level());
        List<ItemStack> pageItems = data.getPage(bagId, page);
        for (int i = 0; i < pageItems.size() && i < customItemHandler.getSlots(); i++) {
            customItemHandler.setStackInSlot(i, pageItems.get(i).copy());
        }
    }

    public BagMenu(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
        super(ModMenus.BAG_MENU.get(), windowId);
        this.bagId = buf.readUUID();
        this.page = buf.readInt();
        this.totalPages = buf.readInt();
        this.customItemHandler = new CustomItemStackHandler(54);

        layoutSlots(playerInv);
    }

    public UUID getBagId() { return bagId; }
    public int getPage() { return page; }
    public int getTotalPages() { return totalPages; }

    private void layoutSlots(Inventory playerInv) {
        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new CustomSlotItemHandler(customItemHandler, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        int playerInvYStart = 140;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, playerInvYStart + row * 18));
            }
        }
        int hotbarY = playerInvYStart + 58;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (!player.level().isClientSide) {
            BagSavedData data = BagSavedData.get(player.level());

            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < customItemHandler.getSlots(); i++) {
                items.add(customItemHandler.getStackInSlot(i).copy());
            }

            data.setPage(bagId, page, items);
            data.setDirty();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalItemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            originalItemstack = stackInSlot.copy();

            int bagStart = 0;
            int bagEnd = 53;
            int playerInvStart = 54;
            int hotbarEnd = 89;

            if (index >= bagStart && index <= bagEnd) {
                if (!this.moveItemStackTo(stackInSlot, playerInvStart, hotbarEnd + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= playerInvStart && index <= hotbarEnd) {
                if (!this.moveItemStackTo(stackInSlot, bagStart, bagEnd + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            ItemStack cursorStack = player.containerMenu.getCarried();
            if (!cursorStack.isEmpty() && index >= bagStart && index <= bagEnd) {
                if (ItemStack.isSameItemSameTags(originalItemstack, cursorStack)) {
                    if (this.moveItemStackTo(cursorStack, bagStart, bagEnd + 1, false)) {
                    }
                }
            }

            if (!player.level().isClientSide) {
                BagSavedData data = BagSavedData.get(player.level());
                List<ItemStack> itemsToSave = new ArrayList<>();
                for (int i = 0; i < this.customItemHandler.getSlots(); i++) {
                    ItemStack stack = this.customItemHandler.getStackInSlot(i);
                    itemsToSave.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                }
                data.setPage(this.bagId, this.page, itemsToSave);
                data.setDirty();
            }
        }

        return originalItemstack;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        super.clicked(slotId, dragType, clickType, player);

        if (!player.level().isClientSide) {
            BagSavedData data = BagSavedData.get(player.level());

            List<ItemStack> itemsToSave = new ArrayList<>();
            for (int i = 0; i < this.customItemHandler.getSlots(); i++) {
                ItemStack stack = this.customItemHandler.getStackInSlot(i);
                itemsToSave.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
            }

            data.setPage(this.bagId, this.page, itemsToSave);
            data.setDirty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void updatePageState(int newPage, List<ItemStack> currentItems) {
        this.page = newPage;

        for (int i = 0; i < this.customItemHandler.getSlots(); i++) {
            ItemStack item = ItemStack.EMPTY;
            if (i < currentItems.size()) {
                item = currentItems.get(i).copy();
            }
            if (item == null) item = ItemStack.EMPTY;
            this.customItemHandler.setStackInSlot(i, item);
        }
    }

    public void setPage(int newPage) {
        this.page = newPage;
        Player player = this.getPlayer();
        if (player != null) {
            BagSavedData data = BagSavedData.get(player.level());
            List<ItemStack> pageItems = data.getPage(this.bagId, this.page);
            for (int i = 0; i < customItemHandler.getSlots(); i++) {
                ItemStack item = ItemStack.EMPTY;
                if (i < pageItems.size()) {
                    item = pageItems.get(i).copy();
                }
                if (item == null) item = ItemStack.EMPTY;
                customItemHandler.setStackInSlot(i, item);
            }
        } else {
            System.err.println("Failed to get player in BagMenu.setPage, cannot update inventory.");
        }
    }

    private Player getPlayer() {
        if (this.slots != null && !this.slots.isEmpty()) {
            for (Slot slot : this.slots) {
                if (slot.container instanceof Inventory playerInventory) {
                    return playerInventory.player;
                }
            }
        }
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}