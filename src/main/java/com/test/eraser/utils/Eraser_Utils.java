package com.test.eraser.utils;

import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.mixin.eraser.TransientEntitySectionManagerAccessor;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.PartEntity;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class Eraser_Utils {
    public static Optional<Entity> findParentEntity(Entity self, double searchRadius) {
        if (self == null || self.level() == null) return Optional.empty();

        AABB box = self.getBoundingBox().inflate(searchRadius);
        List<Entity> nearby = self.level().getEntities(self, box);

        for (Entity e : nearby) {
            if (e == self) continue;

            PartEntity<?>[] parts = e.getParts();
            if (parts != null) {
                for (PartEntity<?> part : parts) {
                    if (part == self) {
                        return Optional.of(e);
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<LivingEntity> findParentLiving(Entity self, double searchRadius) {
        Optional<Entity> parent = findParentEntity(self, searchRadius);
        return parent.filter(p -> p instanceof LivingEntity).map(p -> (LivingEntity) p);
    }

    @SuppressWarnings("unchecked")
    public static Optional<ILivingEntity> findParentILiving(Entity self, double searchRadius) {
        Optional<Entity> parent = findParentEntity(self, searchRadius);
        if (parent.isEmpty()) return Optional.empty();
        Entity p = parent.get();
        if (p instanceof ILivingEntity) {
            return Optional.of((ILivingEntity) p);
        }
        return Optional.empty();
    }

    public static boolean killIfParentFound(Entity self, Entity attacker, int moredrop, double searchRadius) {
        Optional<ILivingEntity> opt = findParentILiving(self, searchRadius);
        if (opt.isPresent()) {
            try {
                if (attacker instanceof Player player)
                    opt.get().instantKill(player);
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (self instanceof ILivingEntity living) living.instantKill((Player) attacker);
        return false;
    }

}
