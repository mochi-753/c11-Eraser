package com.test.eraser.mixin.eraser;

import com.test.eraser.Config;
import com.test.eraser.additional.ModDamageSources;
import com.test.eraser.additional.SnackArmor;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.network.PacketHandler;
import com.test.eraser.network.packets.EraseEntityPacket;
import com.test.eraser.utils.EraseEntityLookupBridge;
import com.test.eraser.utils.SynchedEntityDataUtil;
import com.test.eraser.utils.TaskScheduler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.*;
import net.minecraftforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final Set<UUID> erasedUuids = ConcurrentHashMap.newKeySet();

    @Override
    public void markErased(UUID uuid) {
        erasedUuids.add(uuid);
    }

    @Override
    public boolean isErased(UUID uuid) {
        return erasedUuids.contains(uuid);
    }

    @Override
    public void instantKill(Player attacker) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.level().isClientSide || this.isErased()) return;
        this.setErased(true);
        DamageSource eraseSrc = ModDamageSources.erase(self, attacker);
        EntityDataAccessor<Float> healthId = LivingEntityAccessor.getDataHealthId();
        SynchedEntityDataUtil.forceSet(self.getEntityData(), healthId, 0.0F);
        ((LivingEntityAccessor) self).setLastHurtByPlayer(attacker);
        ((LivingEntityAccessor) self).setLastHurtByPlayerTime((int)Instant.now().getEpochSecond());
        self.getCombatTracker().recordDamage(eraseSrc, Float.MAX_VALUE);

        if (Config.isNormalDieEntity(self)) {}
        else if (Config.FORCE_DIE.get()) {
            forcedie(eraseSrc);
            if (!(self instanceof ServerPlayer)/* && !self.isDeadOrDying()*/)
                TaskScheduler.schedule(this::forceErase, 19);
        }
        //ServerLevel dest = self.getServer().getLevel(Level.OVERWORLD);
        //if (dest == null) return;
        //Entity moved = self.changeDimension(dest);//for muteki star
        //but can kill without calling on ChengeDimention :)
    }

    private void forcedie(DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;
        //if(!(self instanceof ServerPlayer)) { self.die(source);}
        ((LivingEntityAccessor) self).setDeadFlag(true);
        //self.deathTime = 1;
        if (!self.level().isClientSide) {
            if (self instanceof ServerPlayer sp) {
                Component deathMsg = sp.getCombatTracker().getDeathMessage();
                sp.connection.send(new ClientboundPlayerCombatKillPacket(sp.getId(), deathMsg));
                if (self.isDeadOrDying()) sp.server.getPlayerList().broadcastSystemMessage(deathMsg, false);
            }
            //((LivingEntityAccessor) self).invokeDropAllDeathLoot(source);
            ((LivingEntityAccessor)self).invokedropFromLootTable(source,false);
            ((LivingEntityAccessor)self).invokedropExperience();
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
        markErased(self.getUUID());
        self.setPosRaw(Double.NaN, Double.NaN, Double.NaN);
        ((EntityAccessor) self).setRemovalReason(Entity.RemovalReason.KILLED);
        self.setRemoved(Entity.RemovalReason.KILLED);
        if (self.level() instanceof ServerLevel serverLevel) {
            self.stopRiding();
            self.invalidateCaps();
            self.setLevelCallback(EntityInLevelCallback.NULL);
            EntityTickList tickList = ((ServerLevelAccessor) serverLevel).getEntityTickList();
            tickList.remove(self);
            Int2ObjectMap<Entity> active = ((EntityTickListAccessor) tickList).getActive();
            active.remove(self.getId());

            ClientboundRemoveEntitiesPacket removePkt = new ClientboundRemoveEntitiesPacket(new int[]{self.getId()});
            ClientboundBossEventPacket bossRemovePkt = ClientboundBossEventPacket.createRemovePacket(self.getUUID());
            for (ServerPlayer sp : serverLevel.players()) {
                sp.connection.send(removePkt);
                sp.connection.send(bossRemovePkt);
                PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new EraseEntityPacket(self.getId()));
            }

            PersistentEntitySectionManager<Entity> manager =
                    ((ServerLevelAccessor) serverLevel).getEntityManager();
            PersistentEntitySectionManagerAccessor<Entity> acc =
                    (PersistentEntitySectionManagerAccessor<Entity>) manager;

            EntityLookup<Entity> vis = acc.getVisibleEntityStorage();
            ((EraseEntityLookupBridge<Entity>) vis).eraseEntity(self);

            LevelEntityGetter<Entity> getter = acc.getEntityGetter();
            EntityLookup<Entity> vis2 = ((LevelEntityGetterAdapterAccessor<Entity>) getter).getVisibleEntities();
            ((EraseEntityLookupBridge<Entity>) vis2).eraseEntity(self);
            EntitySectionStorage<Entity> storage2 = ((LevelEntityGetterAdapterAccessor<Entity>) getter).getSectionStorage();
            long sectionKey2 = SectionPos.asLong(self.blockPosition());
            EntitySection<Entity> section2 = storage2.getSection(sectionKey2);
            if (section2 != null) {
                ClassInstanceMultiMap<Entity> multiMap =
                        ((EntitySectionAccessor<Entity>) section2).getStorage();
                multiMap.remove(self);
            }
            acc.getKnownUuids().remove(self.getUUID());

            EntitySectionStorage<Entity> storage = acc.getSectionStorage();
            long sectionKey = SectionPos.asLong(self.blockPosition());
            EntitySection<Entity> section = storage.getSection(sectionKey);
            if (section != null) {
                ((EntitySectionAccessor) section).getStorage().remove(self);
                ;
                ClassInstanceMultiMap<Entity> multiMap = ((EntitySectionAccessor<Entity>) section).getStorage();
                Map<Class<?>, List<Entity>> byClass = ((ClassInstanceMultiMapAccessor<Entity>) multiMap).getByClass();
                hardRemove(self, byClass);

            }
            ChunkMap chunkMap = serverLevel.getChunkSource().chunkMap;
            Int2ObjectMap<?> entityMap = ((ChunkMapAccessor) chunkMap).getEntityMap();
            entityMap.remove(self.getId());
            if (self instanceof TrackedEntityAccessor accessor) {
                accessor.invokeBroadcastRemoved();
            }
            if (serverLevel.getEntity(self.getUUID()) != null && vis.getEntity(self.getUUID()) != null && vis2.getEntity(self.getUUID()) != null && acc.getKnownUuids().contains(self.getUUID()) && ((EntitySectionAccessor<?>) section2).getStorage().contains(self)) {
                System.err.println("[EraserMod] failed to fully remove entity id=" + self.getId());
            } else {
                System.out.println("[EraserMod] successfully removed entity id=" + self.getId());
            }
        }

    }

    private static void hardRemove(Entity self, Map<Class<?>, List<Entity>> byClass) {
        Class<?> c = self.getClass();
        List<Entity> list = byClass.get(c);
        if (list != null) {
            list.remove(self);
            if (list.isEmpty()) byClass.remove(c);
        }
        for (Map.Entry<Class<?>, List<Entity>> e : byClass.entrySet()) {
            List<Entity> l = e.getValue();
            if (l != null && !l.isEmpty()) {
                l.remove(self);
                if (l.isEmpty()) byClass.remove(e.getKey());
            }
        }
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

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void eraser$die(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (this.isErased()) {
            ci.cancel();
        }
    }
}


