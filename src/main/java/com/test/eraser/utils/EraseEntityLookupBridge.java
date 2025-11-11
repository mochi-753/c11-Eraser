package com.test.eraser.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Unique;

public interface EraseEntityLookupBridge<T extends EntityAccess> {

    @Unique
    boolean eraseEntity(T entity);
}
