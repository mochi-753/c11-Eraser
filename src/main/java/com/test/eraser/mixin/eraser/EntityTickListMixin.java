package com.test.eraser.mixin.eraser;

import com.test.eraser.logic.ILivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTickList.class)
public abstract class EntityTickListMixin {
    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private void onAdd(Entity entity, CallbackInfo ci) {
        if (entity instanceof ILivingEntity erase && erase.isErased()) {
            ci.cancel();
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity entity, CallbackInfo ci) {
    }
}