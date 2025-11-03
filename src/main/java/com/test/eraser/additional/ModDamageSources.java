package com.test.eraser.additional;

import com.test.eraser.utils.Res;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class ModDamageSources {

    public static DamageSource erase(Entity target, @Nullable Entity attacker) {
        var holder = target.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Res.getResource("eraser", "erase")));

        if (attacker instanceof Player) {
            return new DamageSource(holder, attacker);
        }

        return new DamageSource(holder, attacker);
    }
}
