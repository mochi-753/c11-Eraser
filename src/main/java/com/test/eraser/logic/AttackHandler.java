package com.test.eraser.logic;

import com.test.eraser.Eraser;
import com.test.eraser.additional.ModDamageTypes;
//import com.test.eraser.Mixin.Armor_And_Eraser.LivingEntityAccessor;
//import com.test.eraser.Mixin.LevelEntityGetterAdapterAccessor;
import com.test.eraser.additional.ModItems;
import com.test.eraser.additional.SnackArmor;
import com.test.eraser.entity.HomingArrowEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraft.world.entity.Entity;

import net.minecraftforge.event.entity.living.LivingDeathEvent;

@Mod.EventBusSubscriber(modid = Eraser.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AttackHandler {
    public static int targetEntityId = -1;

    /*public static void instantKill(Entity target, Player attacker) {
        if (target == null || target.level().isClientSide() || !target.isAlive()) return;
        //attacker.sendSystemMessage(Component.literal(target.toString()));
        Entity resolved = target;
        double radius = 64.0;
        AABB area = target.getBoundingBox().inflate(radius);
        List<Entity> nearby = target.level().getEntities(target, area, e -> true);

        for (Entity e : nearby) {
            Entity[] parts = e.getParts();
            if (parts != null) {
                for (Entity part : parts) {
                    if (part == target) {
                        resolved = e;
                        break;
                    }
                }
            }
        }
        if (resolved instanceof LivingEntity living) {
            if (living.isAlive()) targetEntityId = living.getId();
            if (living instanceof IEraserEntity erased) {
                erased.setErased(true);
                erased.setProcessedId(living.getId());
            }
            int reward = living.getExperienceReward();
            if (reward > 0) {
                target
                        .level()
                        .addFreshEntity(
                                new ExperienceOrb(target.level(), target.getX(), target.getY(), target.getZ(), reward)
                        );
            }
            DamageSource erase = ModDamageSources.erase(living, attacker);
            //living.hurt(erase, 1);
            ((LivingEntityAccessor) living).setLastHurtByPlayer(attacker);
            living.setHealth(0.0F);
            living.getCombatTracker().recordDamage(erase, Float.MAX_VALUE);
            living.die(erase);
            living.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            //living.discard();
            //return;

        }

        resolved.kill();
        resolved.discard();
        if (!(resolved instanceof Player player)) forceErase(resolved);
    }

    public static void instantKill(Entity target) {
        if (target == null || target.level().isClientSide() || !target.isAlive()) return;

        Entity resolved = target;
        double radius = 64.0;
        AABB area = target.getBoundingBox().inflate(radius);
        List<Entity> nearby = target.level().getEntities(target, area, e -> true);

        for (Entity e : nearby) {
            Entity[] parts = e.getParts();
            if (parts != null) {
                for (Entity part : parts) {
                    if (part == target) {
                        resolved = e;
                        break;
                    }
                }
            }
        }
        if (resolved instanceof LivingEntity living) {
            if (living.isAlive()) targetEntityId = living.getId();
            if (living instanceof IEraserEntity erased) {
                erased.setErased(true);
                erased.setProcessedId(living.getId());
            }
            int reward = living.getExperienceReward();
            if (reward > 0) {
                target
                        .level()
                        .addFreshEntity(
                                new ExperienceOrb(target.level(), target.getX(), target.getY(), target.getZ(), reward)
                        );
            }
            DamageSource erase = ModDamageSources.erase(living, target);
            living.hurt(erase, 1);
            living.setHealth(0.0F);
            EntityDataAccessor<Float> healthId = LivingEntityAccessor.getDataHealthId();
            living.getEntityData().set(healthId, 0.0F);
            living.getCombatTracker().recordDamage(erase, Float.MAX_VALUE);
            living.die(erase);
            living.remove(Entity.RemovalReason.CHANGED_DIMENSION);
            //living.discard();
            //return;
        }

        resolved.kill();
        resolved.discard();
        if (!(resolved instanceof Player player)) forceErase(resolved);
    }

    public static void forceErase(Entity target) {
        if (target == null || !(target.level() instanceof ServerLevel serverLevel)) return;

        PersistentEntitySectionManager<Entity> manager =
                ((ServerLevelAccessor) serverLevel).getEntityManager();

        EntitySectionStorage<Entity> storage =
                ((PersistentEntitySectionManagerAccessor) manager).getSectionStorage();

        long sectionKey = SectionPos.asLong(target.blockPosition());
        EntitySection<Entity> section = storage.getSection(sectionKey);
        if (section != null) {
            section.remove(target);

            ClassInstanceMultiMap<Entity> multiMap =
                    ((EntitySectionAccessor<Entity>) section).getStorage();

            Map<Class<?>, List<Entity>> map =
                    ((ClassInstanceMultiMapAccessor<Entity>) multiMap).getByClass();

            for (Map.Entry<Class<?>, List<Entity>> entry : map.entrySet()) {
                if (entry.getKey().isInstance(target)) {
                    entry.getValue().remove(target);
                }
            }
        }

        target.setRemoved(Entity.RemovalReason.KILLED);
        target.onRemovedFromWorld();
        target.invalidateCaps();

        ClientboundRemoveEntitiesPacket packet =
                new ClientboundRemoveEntitiesPacket(new int[]{target.getId()});
        for (ServerPlayer sp : serverLevel.players()) {
            sp.connection.send(packet);
        }
    }*/

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().is(ModDamageTypes.ERASE)) {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof Player player) {
                if (event.getEntity() instanceof ILivingEntity player_) player_.instantKill();
            } else if (attacker instanceof ILivingEntity player_) player_.instantKill();
            event.setCanceled(true);
        }
        Entity direct = event.getSource().getDirectEntity();
        if (direct instanceof HomingArrowEntity homing) {
            event.setAmount(0);
            event.setCanceled(true);
        }
        if (event.getEntity() instanceof Player player && SnackArmor.SnackProtector.hasSnackProtector(player)) {
            event.setAmount(0);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();

        if (attacker instanceof Player player) {
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            boolean HasEraser =
                    main.getItem() instanceof com.test.eraser.items.Eraser_Item
                            || off.getItem() instanceof com.test.eraser.items.Eraser_Item;
            if (!HasEraser) return;
            //instantKill(event.getEntity(), player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {

    }

}


