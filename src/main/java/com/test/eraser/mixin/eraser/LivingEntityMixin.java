package com.test.eraser.mixin.eraser;

import com.test.eraser.additional.ModDamageSources;
import com.test.eraser.additional.ModDamageTypeTags;
import com.test.eraser.additional.SnackArmor;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.utils.SynchedEntityDataUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(value = LivingEntity.class, priority = 1000)
public abstract class LivingEntityMixin implements ILivingEntity {

    @Unique
    private boolean erased = false;
    @Unique
    private boolean Fullset = false;

    @Override
    public boolean isErased() {
        return this.erased;
    }

    @Override
    public void setErased(boolean flag) {
        this.erased = flag;
    }

    @Override
    public boolean wasFullset() {
        return this.Fullset;
    }

    @Override
    public void setwasFullset(boolean Fullset) {
        this.Fullset = Fullset;
    }

    @Override
    public void instantKill(Player attacker) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isAlive()) return;
        this.setErased(true);
        DamageSource eraseSrc = ModDamageSources.erase(self, attacker);
        EntityDataAccessor<Float> healthId = LivingEntityAccessor.getDataHealthId();
        SynchedEntityDataUtil.forceSet(self.getEntityData(), healthId, 0.0F);
        ((LivingEntityAccessor) self).setLastHurtByPlayer(attacker);
        ((LivingEntityAccessor) self).setLastHurtByPlayerTime((int) Instant.now().getEpochSecond());
        self.getCombatTracker().recordDamage(eraseSrc, Float.MAX_VALUE);


        forcedie(eraseSrc);
        if (!(self instanceof Player)) forceErase();


        //ServerLevel dest = self.getServer().getLevel(Level.OVERWORLD);
        //if (dest == null) return;
        //Entity moved = self.changeDimension(dest);//for muteki star
        //but can kill without calling on ChengeDimention :)
    }

    private void forcedie(DamageSource source) {

        LivingEntity self = (LivingEntity) (Object) this;
        if(!(self instanceof ServerPlayer))self.die(source);
        ((LivingEntityAccessor) self).setDeadFlag(true);
        if (!self.level().isClientSide) {
            if (self instanceof ServerPlayer sp) {
                Component deathMsg = sp.getCombatTracker().getDeathMessage();
                sp.connection.send(new ClientboundPlayerCombatKillPacket(sp.getId(), deathMsg));
                if (((LivingEntityAccessor) self).isDeadFlag())
                    sp.server.getPlayerList().broadcastSystemMessage(deathMsg, false);
            }
            ((LivingEntityAccessor) self).invokeDropAllDeathLoot(source);
        }
        self.setPose(Pose.DYING);
    }

    @Override
    public void instantKill() {
        instantKill((Player) null);
    }

    @Override
    public void forceErase() {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self.level() instanceof ServerLevel serverLevel)) return;
        try {
            self.setRemoved(Entity.RemovalReason.KILLED);
            self.stopRiding();
            self.onRemovedFromWorld();
            self.invalidateCaps();
            /*if (self instanceof Mob mob) {
                mob.setNoAi(true);
                mob.goalSelector.getAvailableGoals().clear();
                mob.targetSelector.getAvailableGoals().clear();
            }*/
            ClientboundBossEventPacket pkt = ClientboundBossEventPacket.createRemovePacket(self.getUUID());
            for (ServerPlayer sp : serverLevel.players()) {
                sp.connection.send(pkt);
            }

            self.setBoundingBox(new AABB(0, 0, 0, 0, 0, 0));
            EntityTickList tickList = ((ServerLevelAccessor) serverLevel).getEntityTickList();
            Int2ObjectMap<Entity> active = ((EntityTickListAccessor) tickList).getActive();
            active.remove(self.getId());
            ClientboundRemoveEntitiesPacket packet =
                    new ClientboundRemoveEntitiesPacket(new int[]{self.getId()});
            for (ServerPlayer sp : serverLevel.players()) {
                sp.connection.send(packet);
            }

            PersistentEntitySectionManager<Entity> manager =
                    ((ServerLevelAccessor) serverLevel).getEntityManager();
            EntitySectionStorage<Entity> storage =
                    ((PersistentEntitySectionManagerAccessor) manager).getSectionStorage();
            ((PersistentEntitySectionManagerAccessor<Entity>) manager)
                    .getCallbacks()
                    .onDestroyed(self);

            EntityLookup<Entity> lookup =
                    ((PersistentEntitySectionManagerAccessor<Entity>) manager).getVisibleEntityStorage();

            Int2ObjectMap<Entity> byId = ((EntityLookupAccessor<Entity>) lookup).getById();
            byId.remove(self.getId());
            Map<UUID, Entity> byUuid = ((EntityLookupAccessor<Entity>) lookup).getByUuid();
            byUuid.remove(self.getUUID());

            Object getter = ((PersistentEntitySectionManagerAccessor<?>) manager).getEntityGetter();
            ((LevelEntityGetterAdapterAccessor<Entity>) getter).getVisibleEntities().remove(self);

            LevelCallback<Entity> callbacks = (LevelCallback<Entity>) ((PersistentEntitySectionManagerAccessor<?>) manager).getCallbacks();
            callbacks.onTrackingEnd(self);
            ((PersistentEntitySectionManagerAccessor<Entity>) manager)
                    .getKnownUuids()
                    .remove(self.getUUID());

            self.setLevelCallback(EntityInLevelCallback.NULL);
            long sectionKey = SectionPos.asLong(self.blockPosition());
            EntitySection<Entity> section = storage.getSection(sectionKey);
            if (section != null) {
                ((PersistentEntitySectionManagerAccessor<Entity>) manager).getSectionStorage().remove(sectionKey);
                ClassInstanceMultiMap<Entity> multiMap =
                        ((EntitySectionAccessor<Entity>) section).getStorage();
                Map<Class<?>, List<Entity>> map =
                        ((ClassInstanceMultiMapAccessor<Entity>) multiMap).getByClass();

                removeEntity(self, map);
            }
            if (serverLevel.getEntity(self.getUUID()) != null) {
                System.err.println("[EraserMod] failed to fully remove entity id=" + self.getId());
            }
        } catch (Throwable t) {
            System.err.print("[EraserMod] forceErase failed for entity id=" + self.getId() + "uuid=" + self.getUUID() + "class={}" + self.getClass().getName() + ":" + t.toString());
            throw t;
        }

    }

    @Override
    public <T> boolean removeEntity(T target, Map<Class<?>, List<T>> storage) {
        boolean removed = false;

        for (Map.Entry<Class<?>, List<T>> entry : storage.entrySet()) {
            Class<?> clazz = entry.getKey();
            List<T> list = entry.getValue();

            if (clazz.isInstance(target)) {
                removed |= list.remove(target);
            }
        }

        return removed;
    }

    @Inject(method = "getHealth", at = @At("HEAD"), cancellable = true)
    private void overrideGetHealth(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (this.isErased()) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    private void overridegetMaxHealth(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (this.isErased()) {
            cir.setReturnValue(0F);
        }
    }

    /*@Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeHooks;onLivingTick(Lnet/minecraft/world/entity/LivingEntity;)Z"
            )
    )
    private boolean eraser$skipLivingTick(LivingEntity entity) {
        if (entity instanceof IEraserEntity erased && (erased.isErased() || erased.getProcessedId() == entity.getId() || true)) {
            entity.sendSystemMessage(Component.literal(
                    "[EraserMod] Skipped LivingTickEvent for " + entity.getName().getString() + " ID: " + entity.getId()
            ));
            System.out.println("[EraserMod] Skipped LivingTickEvent for " + entity.getName().getString() + " ID: " + entity.getId());
            entity.setHealth(0);
            //return true;
        }
        return ForgeHooks.onLivingTick(entity);
    }*/


    @Inject(method = "isAlive", at = @At("HEAD"), cancellable = true)
    private void eraser$isAlive(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player player && SnackArmor.SnackProtector.isFullSet(player)) {
            cir.setReturnValue(true);
            return;
        }
        if (this.isErased()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isDeadOrDying", at = @At("HEAD"), cancellable = true)
    private void eraser$isDeadOrDying(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (this.isErased()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void eraser$shrinkAABBOnTick(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (this.isErased()) {
            //self.setBoundingBox(new AABB(self.getX(), self.getY(), self.getZ(), self.getX(), self.getY(), self.getZ()));
            //ci.cancel();
        }
    }
}


