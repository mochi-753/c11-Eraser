package com.test.eraser.mixin.client;

import com.test.eraser.logic.ILivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(
            method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;)V",
            at = @At("TAIL")
    )
    private <T extends Entity> void filterEntities(
            EntityTypeTest<Entity, T> type,
            AABB box,
            Predicate<? super T> predicate,
            List<? super T> output,
            CallbackInfo ci
    ) {
        output.removeIf(e -> e instanceof ILivingEntity living && living.isErased());
    }
}