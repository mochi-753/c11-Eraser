package com.test.eraser.gui;

import com.test.eraser.network.PacketHandler;
import com.test.eraser.network.packets.ChangeBagPagePacket;
import com.test.eraser.network.packets.SortBagPacket;
import com.test.eraser.utils.ClientBagCache;
import com.test.eraser.utils.CustomItemStackHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

@OnlyIn(Dist.CLIENT)
public class ClientBagGui extends AbstractContainerScreen<BagMenu> {
    private int currentPage;
    private UUID bagId;

    private List<ItemStack> prevPage = Collections.emptyList();
    private List<ItemStack> nextPage = Collections.emptyList();
    private List<ItemStack> currentPageItems = Collections.emptyList();

    private Button prevButton;
    private Button nextButton;
    private Button sortByNameButton;

    private EditBox searchBox;
    private String lastSearchText = "";
    private Set<Integer> matchingSlots = new HashSet<>();

    public ClientBagGui(BagMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();
        ClientBagCache.CachedBagData cached = ClientBagCache.get(this.menu.getBagId());
        if (cached != null) {
            this.update(this.menu.getBagId(),
                    cached.page(),
                    cached.prev(),
                    cached.current(),
                    cached.next());
        }

        int buttonHeight = 18;
        int buttonWidth = 18;
        int searchBoxHeight = 14;
        int labelY = this.topPos + 6;

        int searchBoxY = labelY + (6 - searchBoxHeight) / 2;
        if (searchBoxY < this.topPos) searchBoxY = this.topPos;

        int searchBoxWidth = 80;
        int arrowButtonX = this.leftPos + this.imageWidth - 20;
        int offset = (buttonWidth * 2) + 10;
        int newSearchBoxX = arrowButtonX - offset;

        int searchBoxX = newSearchBoxX - searchBoxWidth - 2;

        this.searchBox = new EditBox(this.font, searchBoxX, searchBoxY, searchBoxWidth, searchBoxHeight, Component.literal("Search..."));
        this.searchBox.setMaxLength(32767);
        this.searchBox.setBordered(true);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(this.searchBox);

        int buttonY = labelY + (6 - buttonHeight) / 2;
        if (buttonY < this.topPos) buttonY = this.topPos;

        int nextButtonX = arrowButtonX - buttonWidth;
        int prevButtonX = nextButtonX - buttonWidth;

        this.prevButton = Button.builder(Component.literal("<"), button -> {
            if (this.bagId != null && this.currentPage > 0) {
                PacketHandler.CHANNEL.sendToServer(new ChangeBagPagePacket(this.bagId, this.currentPage - 1));
            } else {
                System.out.println("Prev button pressed: bagId is null or currentPage <= 0. bagId: " + this.bagId + ", currentPage: " + this.currentPage);
            }
        }).pos(prevButtonX, buttonY).size(buttonWidth, buttonHeight).build();

        this.nextButton = Button.builder(Component.literal(">"), button -> {
            if (this.bagId != null && this.currentPage < this.menu.getTotalPages() - 1) {
                PacketHandler.CHANNEL.sendToServer(new ChangeBagPagePacket(this.bagId, this.currentPage + 1));
            } else {
                System.out.println("Next button pressed: bagId is null or currentPage >= totalPages - 1. bagId: " + this.bagId + ", currentPage: " + this.currentPage + ", totalPages: " + this.menu.getTotalPages());
            }
        }).pos(nextButtonX, buttonY).size(buttonWidth, buttonHeight).build();

        int sortButtonX = prevButtonX - buttonWidth - 2;

        this.sortByNameButton = Button.builder(Component.literal("N"), button -> {
                    PacketHandler.CHANNEL.sendToServer(new SortBagPacket(this.bagId, SortBagPacket.SortType.NAME));
                    if (this.bagId != null && this.currentPage < this.menu.getTotalPages() - 1)
                        PacketHandler.CHANNEL.sendToServer(new ChangeBagPagePacket(this.bagId, this.currentPage));//update page
                })
                .pos(sortButtonX, buttonY)
                .size(buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.literal("Sort by Name")))
                .build();

        this.addRenderableWidget(this.prevButton);
        this.addRenderableWidget(this.nextButton);
        this.addRenderableWidget(this.sortByNameButton);

        updateSearchResults();
        /*this.prevButton.active = false;
        this.nextButton.active = false;*/
        updateButtonVisibility();
    }

    private String formatCount(long count) {
        if (count < 1000) {
            return String.valueOf(count);
        }
        // 'B' Billion 1,000,000,000 (10^9)
        // 'M' Million 1,000,000 (10^6)
        // 'k' kilo 1,000 (10^3)
        if (count >= 1_000_000_000L) {
            return String.format("%.1fB", count / 1_000_000_000.0);
        } else if (count >= 1_000_000L) {
            return String.format("%.1fM", count / 1_000_000.0);
        } else if (count >= 1_000L) {
            return String.format("%.1fk", count / 1_000.0);
        }
        return String.valueOf(count);
    }

    public void update(UUID bagId, int page,
                       List<ItemStack> prev, List<ItemStack> current, List<ItemStack> next) {
        this.bagId = bagId;
        this.currentPage = page;
        this.prevPage = prev;
        this.currentPageItems = current;
        this.nextPage = next;

        CustomItemStackHandler handler = this.menu.customItemHandler;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack item = ItemStack.EMPTY;
            if (i < currentPageItems.size()) {
                item = currentPageItems.get(i);
            }
            if (item == null) item = ItemStack.EMPTY;

            handler.setStackInSlot(i, item);
        }
        updateButtonVisibility();
        updateSearchResults();
    }

    private void updateButtonVisibility() {
        if (this.prevButton != null) {
            this.prevButton.visible = this.currentPage > 0;
        }
        if (this.nextButton != null) {
            this.nextButton.visible = this.currentPage < this.menu.getTotalPages() - 1;
        }
    }

    @SuppressWarnings("removal")
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(new ResourceLocation("textures/gui/container/generic_54.png"),
                this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderLabels(guiGraphics, mouseX, mouseY);
        this.searchBox.render(guiGraphics, mouseX, mouseY, partialTicks);

        for (int slotIndex : this.matchingSlots) {
            if(Objects.equals(lastSearchText, ""))break;
            if (slotIndex >= 0 && slotIndex < 54) {
                Slot slot = this.menu.getSlot(slotIndex);

                int slotX = this.leftPos + 8 + (slotIndex % 9) * 18; // col
                int slotY = this.topPos + 18 + (slotIndex / 9) * 18; // row
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x8000FF00);
            }
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);

        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, 130, 0x404040, false);
    }

    private void onSearchChanged(String newText) {
        this.lastSearchText = newText;
        updateSearchResults();
    }

    private void updateSearchResults() {
        this.matchingSlots.clear();
        String searchTextLower = this.lastSearchText.toLowerCase();

        if (searchTextLower.isEmpty()) {
            for (int i = 0; i < 54; i++) {
                this.matchingSlots.add(i);
            }
            return;
        }

        for (int i = 0; i < 54; i++) {
            Slot slot = this.menu.getSlot(i);
            if (slot.hasItem()) {
                ItemStack stack = slot.getItem();
                String displayName = stack.getHoverName().getString().toLowerCase();
                if (displayName.contains(searchTextLower)) {
                    this.matchingSlots.add(i);
                }
            }
        }
    }

}