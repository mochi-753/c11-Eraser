package com.test.eraser.mixin.eraser;

import com.test.eraser.Config;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.utils.EraseEntityLookupBridge;
import com.test.eraser.utils.SynchedEntityDataUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @Inject(method = "write", at = @At("HEAD"))
    private static void onWrite(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir) {
        PersistentEntitySectionManager<Entity> manager =
                ((ServerLevelAccessor) level).getEntityManager();
        PersistentEntitySectionManagerAccessor<Entity> acc =
                (PersistentEntitySectionManagerAccessor<Entity>) manager;

        EntitySectionStorage<Entity> storage = acc.getSectionStorage();

        storage.getAllChunksWithExistingSections().forEach(sectionKey -> {
            EntitySection<Entity> section = storage.getSection(sectionKey);
            if (section != null) {
                ClassInstanceMultiMap<Entity> multiMap =
                        ((EntitySectionAccessor<Entity>) section).getStorage();

                multiMap.removeIf(e -> (e instanceof ILivingEntity living) && living.isErased());
            }
        });
        acc.getKnownUuids().removeIf(uuid -> {
            Entity e = level.getEntity(uuid);
            if((e instanceof ILivingEntity living) && living.isErased() && !Config.isNormalDieEntity(e)) System.out.println("[Eraser] Removed entity UUID from knownUuids: " + uuid);
            return (e instanceof ILivingEntity living) && living.isErased();
        });

    }

    @Inject(method = "read", at = @At("HEAD"))
    private static void onRead(ServerLevel level, PoiManager poiManager, ChunkPos pos, CompoundTag tag, CallbackInfoReturnable<ProtoChunk> cir) {
    }
}