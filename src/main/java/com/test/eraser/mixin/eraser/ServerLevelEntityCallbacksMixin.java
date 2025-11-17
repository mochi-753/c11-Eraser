package com.test.eraser.mixin.eraser;

import com.test.eraser.logic.ILivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks", priority = 0)
public class ServerLevelEntityCallbacksMixin {
    @Inject(method = "onTrackingStart", at = @At("HEAD"), cancellable = true)
    private void onTrackingStart(Entity entity, CallbackInfo ci) {
        if (entity instanceof ILivingEntity erase && erase.isErased()) {
            //ci.cancel();
        }
    }
}