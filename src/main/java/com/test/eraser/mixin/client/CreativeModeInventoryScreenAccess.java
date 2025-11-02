package com.test.eraser.mixin.client;

import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenAccess {
    @Accessor(value = "selectedTab", remap = true)
    static CreativeModeTab getSelectedTab() {
        throw new AssertionError();
    }

}
