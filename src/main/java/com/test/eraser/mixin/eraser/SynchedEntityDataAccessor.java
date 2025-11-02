package com.test.eraser.mixin.eraser;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SynchedEntityData.class)
public interface SynchedEntityDataAccessor {
    @Invoker("getItem")
    <T> SynchedEntityData.DataItem<T> invokeGetItem(EntityDataAccessor<T> accessor);

    @Accessor("entity")
    Entity getEntity();

    @Accessor("isDirty")
    void setDirtyFlag(boolean dirty);
}
