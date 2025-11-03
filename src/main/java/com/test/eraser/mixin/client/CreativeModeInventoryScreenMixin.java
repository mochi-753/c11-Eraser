package com.test.eraser.mixin.client;

import com.test.eraser.additional.ModCreativeTabs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*"CreativeModeInventoryScreenMixin",
    "CreativeModeInventoryScreenAccess",*/
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin
        extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    private static int waveGrayWhiteColor(long time, int index, double speed) {
        double wave = (Math.sin((time / speed) + index) + 1.0) / 2.0;
        int gray = 0xAAAAAA;
        int white = 0xFFFFFF;
        int r = (int) (((gray >> 16) & 0xFF) * (1 - wave) + ((white >> 16) & 0xFF) * wave);
        int g = (int) (((gray >> 8) & 0xFF) * (1 - wave) + ((white >> 8) & 0xFF) * wave);
        int b = (int) ((gray & 0xFF) * (1 - wave) + (white & 0xFF) * wave);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    @Inject(method = "renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("HEAD"), cancellable = true)
    private void injectRenderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        CreativeModeTab currentTab = CreativeModeInventoryScreenAccess.getSelectedTab();
        if (currentTab == null || !ModCreativeTabs.ERASER_TAB.isPresent()) return;
        if (currentTab != ModCreativeTabs.ERASER_TAB.get()) return;

        ci.cancel();

        if (currentTab.showTitle()) {
            long time = Minecraft.getInstance().level.getGameTime();
            String text = currentTab.getDisplayName().getString();
            int x = 8;
            int y = 6;

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                int color = waveGrayWhiteColor(time, i, 6.0);
                guiGraphics.drawString(this.font, String.valueOf(c), x, y, color, false);
                x += this.font.width(String.valueOf(c));
            }
        }
    }
}

