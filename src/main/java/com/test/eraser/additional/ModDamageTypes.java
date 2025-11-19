package com.test.eraser.additional;

import com.test.eraser.Eraser;
import com.test.eraser.utils.Res;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> ERASE =
            ResourceKey.create(Registries.DAMAGE_TYPE, Res.getResource(Eraser.MODID, "erase"));
}
