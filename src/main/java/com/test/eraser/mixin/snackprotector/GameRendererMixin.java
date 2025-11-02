package com.test.eraser.mixin.snackprotector;

import com.test.eraser.additional.SnackArmor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
    private static void eraser$forceNightVision(LivingEntity entity, float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (entity instanceof Player player) if (SnackArmor.SnackProtector.isFullSet(player)) {
            cir.setReturnValue(1.0F);
            cir.cancel();
            return;
        }
    }
}
