package com.test.eraser.additional;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class ModTiers {
    public static final Tier WORLD_DESTROYER_TIER = new ForgeTier(
            1919, // harvestLevel
            0, // durability
            5000000000000.0F, // miningSpeed
            Integer.MAX_VALUE, // attackDamageBonus
            30, // enchantmentValue
            BlockTags.NEEDS_DIAMOND_TOOL,
            () -> Ingredient.EMPTY
    );
    public static final Tier ERASER_TIER = new ForgeTier(
            1919,
            0,
            35.0F,
            Integer.MAX_VALUE,
            30,
            BlockTags.NEEDS_DIAMOND_TOOL,
            () -> Ingredient.EMPTY
    );

}
