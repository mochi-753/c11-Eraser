package com.test.eraser.mixin.eraser;

import com.test.eraser.logic.ILivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeEventFactory.class)
public class ForgeEventFactoryMixin {
    @Inject(
            method = "onPlayerPreTick",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void eraser$cancelPreTick(Player player, CallbackInfo ci) {
        if (player instanceof ILivingEntity erased && erased.isErased()) {
            player.setHealth(0);
            //ci.cancel();
        }
    }

    @Inject(
            method = "onPlayerPostTick",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void eraser$cancelPostTick(Player player, CallbackInfo ci) {
        if (player instanceof ILivingEntity erased && erased.isErased()) {
            //ci.cancel();
        }
    }
}
