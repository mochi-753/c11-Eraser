package com.test.eraser.mixin.client;

import com.test.eraser.additional.SnackArmor;
import com.test.eraser.logic.ILivingEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class SetScreenMixin {//for witherzilla

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (Minecraft.getInstance() == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if ((SnackArmor.SnackProtector.isFullSet(mc.player) && mc.player.isAlive()) && !((ILivingEntity) mc.player).isErased(mc.player.getUUID()))
            if (screen instanceof DeathScreen) {
                ci.cancel();
            }
    }
}