package com.test.eraser.mixin.eraser;

import com.test.eraser.additional.ModItems;
import com.test.eraser.logic.ILivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {
    @Inject(
            method = "onLivingTick",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void eraser$cancelLivingTick(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ILivingEntity erased && (erased.isErased())) {
            /*entity.sendSystemMessage(Component.literal(
                    "[EraserMod] Skipped LivingTickEvent for " + entity.getName().getString() + " ID: " + entity.getId()
            ));*/
            //System.out.println("[EraserMod] ForgeHooks.onLivingTick skipped for " + entity.getName().getString());
            entity.setHealth(0);
            //AttackHandler.instantKill(entity);
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "onLivingDeath",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void eraser$cancelLivingDeath(LivingEntity entity, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ILivingEntity erased && erased.isErased()) {
            //System.out.println("[EraserMod] Skipped LivingDeathEvent for " + entity.getName().getString());
            entity.setHealth(0);
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "getLootingLevel(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void onGetLootingLevel(Entity target, Entity killer, DamageSource cause, CallbackInfoReturnable<Integer> cir) {
        if (killer instanceof Player player) {
            ItemStack main = player.getMainHandItem();
            ItemStack off  = player.getOffhandItem();

            boolean hasEraser = !main.isEmpty() && main.getItem() == ModItems.ERASER_ITEM.get()
                    || !off.isEmpty()  && off.getItem()  == ModItems.ERASER_ITEM.get();

            boolean hasWorldDestroyer = !main.isEmpty() && main.getItem() == ModItems.WORLD_DESTROYER.get()
                    || !off.isEmpty()  && off.getItem()  == ModItems.WORLD_DESTROYER.get();

            if (hasEraser || hasWorldDestroyer) {
                cir.setReturnValue(7);
            }
        }
    }


    /*@Inject(
            method = "onPlayerPreTick",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void eraser$cancelPreTick(Player player, CallbackInfo ci) {
        if (player instanceof IEraserEntity erased && erased.isErased()) {
            ci.cancel();//@MoreMekaSuitModules hehehe
        }
    }*/

    /*@Inject(
            method = "onPlayerTick",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void eraser$cancelTick(Player player, CallbackInfo ci) {
        if (player instanceof IEraserEntity erased && erased.isErased()) {
            ci.cancel();
        }
    }*/
}