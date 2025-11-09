package com.test.eraser.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {
    @Accessor("entityStorage")
    TransientEntitySectionManager<Entity> getTransientEntityManager();

}