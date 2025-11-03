package com.test.eraser.mixin.eraser;

import net.minecraft.world.level.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TransientEntitySectionManager.class)
public interface TransientEntitySectionManagerAccessor<T extends EntityAccess> {
    @Accessor("sectionStorage")
    EntitySectionStorage<T> getSectionStorage();

    @Accessor("callbacks")
    LevelCallback<T> getCallbacks();

    @Invoker("removeSectionIfEmpty")
    void invokeRemoveSectionIfEmpty(long sectionKey, EntitySection<T> section);

}