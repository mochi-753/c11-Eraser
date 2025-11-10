package com.test.eraser.mixin.eraser;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("removalReason")
    Entity.RemovalReason getRemovalReason();

    @Accessor("removalReason")
    void setRemovalReason(Entity.RemovalReason reason);

    @Accessor("levelCallback")
    EntityInLevelCallback getLevelCallBack();

    @Accessor("levelCallback")
    void setlevelCallback(EntityInLevelCallback callback);
}
