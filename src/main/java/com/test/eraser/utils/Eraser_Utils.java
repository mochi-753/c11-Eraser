package com.test.eraser.utils;

import com.test.eraser.logic.ILivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class Eraser_Utils {
    public static Entity findParentEntity(Entity self, double searchRadius) {
        if (self == null || self.level() == null) return null;

        AABB box = self.getBoundingBox().inflate(searchRadius);
        List<Entity> nearby = self.level().getEntities(self, box);

        for (Entity e : nearby) {
            if (e == self) continue;

            PartEntity<?>[] parts = e.getParts();
            if (parts != null) {
                for (PartEntity<?> part : parts) {
                    if (part == self) {
                        return e;
                    }
                }
            }
        }

        return null;
    }

    public static boolean killIfParentFound(Entity self, Entity attacker, double searchRadius) {
        if (findParentEntity(self, searchRadius) instanceof ILivingEntity entity) {

            if (attacker instanceof Player player) {
                entity.instantKill(player, false);
                return true;
            }
        }
        if(self instanceof ILivingEntity entity && attacker instanceof Player player)
            entity.instantKill(player, false);

        return false;
    }

    public static boolean killIfParentFound(Entity self, Entity attacker, double searchRadius, boolean skipAnimation) {
        if (findParentEntity(self, searchRadius) instanceof ILivingEntity entity) {

            if (attacker instanceof Player player) {
                entity.instantKill(player, skipAnimation);
                return true;
            }
        }
        if(self instanceof ILivingEntity entity && attacker instanceof Player player)
            entity.instantKill(player, skipAnimation);

        return false;
    }
}

