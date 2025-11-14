package com.test.eraser.mixin.eraser;

import com.test.eraser.Config;
import com.test.eraser.additional.ModDamageTypes;
import com.test.eraser.additional.SnackArmor;
import com.test.eraser.logic.ILivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = Entity.class, priority = 500)
public class EntityMixin {

    @Inject(method = "shouldBeSaved", at = @At("HEAD"), cancellable = true)
    private void eraseGuard(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        if (self instanceof ILivingEntity erase && erase.isErased()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableto(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity)(Object)this;
        boolean iserase = source.is(ModDamageTypes.ERASE);
        if(iserase) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(
            method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;",
            at = @At("HEAD"),
            cancellable = false
    )
    private void onChangeDimensionHead(ServerLevel pDestination, CallbackInfoReturnable<Entity> cir) {
        Entity ent = (Entity) (Object) this;

        if (ent.level().isClientSide()) return;
        if (!(ent instanceof LivingEntity living)) return;
        if (!(ent instanceof ILivingEntity self)) return;
        //if(self.isErased()) living.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if ((Object)this instanceof ILivingEntity living && living.isErased()) {
            if (!Config.isNormalDieEntity(((Entity)((Object)this))))ci.cancel();
        }
    }

    @Inject(method = "isRemoved", at = @At("HEAD"), cancellable = true)
    private void onisRemoved(CallbackInfoReturnable<Boolean> cir) {
        if (((Entity)(Object)this) instanceof ILivingEntity living && living.isErased()) {
            cir.setReturnValue(true);
        }
    }

    /*@Inject(method = "getId", at = @At("HEAD"), cancellable = true)
    private void overridegetId(CallbackInfoReturnable<Integer> cir) {
        Entity ent = (Entity) (Object) this;
        if (!(ent instanceof LivingEntity living)) return;
        if (!(living instanceof ILivingEntity self)) return;
        if (self.isErased() || (self.isErased() && self instanceof Player player && !SnackArmor.SnackProtector.isFullSet(player))) {
            cir.setReturnValue(-1);
        }
    }*/

}