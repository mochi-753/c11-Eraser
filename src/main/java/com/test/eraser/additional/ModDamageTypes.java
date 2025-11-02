package com.test.eraser.additional;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> ERASE =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("eraser", "erase"));
}
