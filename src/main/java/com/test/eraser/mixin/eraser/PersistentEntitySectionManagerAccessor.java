package com.test.eraser.mixin.eraser;

import net.minecraft.world.level.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import java.util.UUID;

@Mixin(PersistentEntitySectionManager.class)
public interface PersistentEntitySectionManagerAccessor<T extends EntityAccess> {
    @Accessor("sectionStorage")
    EntitySectionStorage<T> getSectionStorage();

    @Accessor("callbacks")
    LevelCallback<T> getCallbacks();

    @Accessor("visibleEntityStorage")
    EntityLookup<T> getVisibleEntityStorage();

    @Accessor("knownUuids")
    Set<UUID> getKnownUuids();

    @Accessor("entityGetter")
    LevelEntityGetter<T> getEntityGetter();
}

