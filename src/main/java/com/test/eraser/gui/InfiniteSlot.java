package com.test.eraser.gui;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container; // 注意: net.minecraft.world.entity.player.Container ではない
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InfiniteSlot extends Slot {
    public InfiniteSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Override
    public boolean mayPlace(ItemStack pStack) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getMaxStackSize();
    }

    @Override
    public void set(ItemStack stack) {

        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        super.set(stack);
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        return true;
    }
}