package com.test.eraser.additional;

import com.test.eraser.utils.Res;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModDamageTypeTags extends TagsProvider<DamageType> {
    public ModDamageTypeTags(PackOutput output,
                             CompletableFuture<HolderLookup.Provider> lookupProvider,
                             ExistingFileHelper helper) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, "eraser", helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<DamageType> damageLookup = provider.lookupOrThrow(Registries.DAMAGE_TYPE);

        ResourceKey<DamageType> eraseKey = ResourceKey.create(Registries.DAMAGE_TYPE, Res.getResource("eraser", "erase"));

        Holder.Reference<DamageType> eraseHolder = damageLookup.getOrThrow(eraseKey);

        this.tag(DamageTypeTags.BYPASSES_ARMOR).add(eraseHolder.key());
        this.tag(DamageTypeTags.BYPASSES_SHIELD).add(eraseHolder.key());
        this.tag(DamageTypeTags.BYPASSES_INVULNERABILITY).add(eraseHolder.key());
        this.tag(DamageTypeTags.BYPASSES_COOLDOWN).add(eraseHolder.key());
        this.tag(DamageTypeTags.BYPASSES_EFFECTS).add(eraseHolder.key());
        this.tag(DamageTypeTags.BYPASSES_RESISTANCE).add(eraseHolder.key());
        this.tag(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(eraseHolder.key());
        this.tag(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS).add(eraseHolder.key());
    }
}
