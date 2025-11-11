package com.test.eraser.mixin.client;

import com.test.eraser.client.ClientEvents;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.mixin.eraser.EntityAccessor;
import com.test.eraser.mixin.eraser.LevelEntityGetterAdapterAccessor;
import com.test.eraser.utils.EraseEntityLookupBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mojang.text2speech.Narrator.LOGGER;

@OnlyIn(Dist.CLIENT)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntity {
    // "client.ClientLevelAccessor",
    @Override
    public void eraseClientEntity() {
        LivingEntity self = (LivingEntity) (Object) this;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel clientLevel = mc.level;
        TransientEntitySectionManager<Entity> tManager = ((ClientLevelAccessor) clientLevel).getTransientEntityManager();
        //My brain is suck
        /*TransientEntitySectionManagerAccessor tAcc = (TransientEntitySectionManagerAccessor) tManager;
        LevelCallback<EntityAccess> cb = ((TransientEntitySectionManagerAccessor) tManager).getCallbacks();
        cb.onTickingEnd(self);
        cb.onTrackingEnd(self);
        long tSectionKey = SectionPos.asLong(self.blockPosition());
        EntitySection<Entity> tSection = tAcc.getSectionStorage().getSection(tSectionKey);
        if (tSection != null) {
            ((EntitySectionAccessor) tSection).getStorage().remove(self);
            ClassInstanceMultiMap<Entity> multiMap = ((EntitySectionAccessor<Entity>) tSection).getStorage();
            Map<Class<?>, List<Entity>> byClass = ((ClassInstanceMultiMapAccessor<Entity>) multiMap).getByClass();
            tSection.remove(self);
            multiMap.remove(self);
            hardRemove(self, byClass);
        }

        EntityLookup<Entity> tVisible =
                ((LevelEntityGetterAdapterAccessor<Entity>) tManager.getEntityGetter()).getVisibleEntities();
        ((EraseEntityLookupBridge<Entity>) tVisible).eraseEntity(self);

        EntitySectionStorage tSectionStrage =
                ((LevelEntityGetterAdapterAccessor<Entity>) tManager.getEntityGetter()).getSectionStorage();
        if (tSectionStrage != null) {
            ClassInstanceMultiMap<Entity> multiMap =
                    ((EntitySectionAccessor<Entity>) tSectionStrage.getSection(tSectionKey)).getStorage();
            EntitySection<LivingEntity> section = tSectionStrage.getSection(tSectionKey);
            section.remove(self);
            Map<Class<?>, List<Entity>> byClass = ((ClassInstanceMultiMapAccessor<Entity>) multiMap).getByClass();
            multiMap.remove(self);
            hardRemove(self, byClass);
        }*/
        self.onClientRemoval();
        ((EntityAccessor)(self)).setRemovalReason(Entity.RemovalReason.KILLED);
        //removeFromOtherIndexes(self.getUUID(), clientLevel, tManager);
        clientLevel.removeEntity(self.getId(), Entity.RemovalReason.KILLED);
        self.remove(Entity.RemovalReason.KILLED);
        Entity e = clientLevel.getEntity(self.getId());
        List<Entity> snapshot = StreamSupport.stream(((LevelEntityGetterAdapterAccessor<Entity>) tManager.getEntityGetter()).getVisibleEntities().getAllEntities().spliterator(), false)
                .collect(Collectors.toList());

        Entity found = null;
        for (Entity ent : snapshot) {

            if (ent == null) continue;
            if (ent.getUUID().equals(self.getUUID())) {
                found = ent;
                LOGGER.info("[EraserMod] still found client entity UUID={} id={} class={}", ent.getUUID(), ent.getId(), ent.getClass().getSimpleName());
                break;
            }
        }
        List<LivingEntity> candidates = self.level().getEntitiesOfClass(LivingEntity.class, self.getBoundingBox()
        );
        for (Entity ent : candidates) {

            if (ent == null) continue;
            if (ent.getUUID().equals(self.getUUID())) {
                found = ent;
                LOGGER.info("[EraserMod] FOUND in collection uuid={} id={} class={} pos={} sectionKey={}",
                        ent.getUUID(),
                        ent.getId(),
                        ent.getClass().getName(),
                        ent.blockPosition(),
                        SectionPos.asLong(ent.blockPosition())
                );

                break;
            }
        }
        self.remove(Entity.RemovalReason.KILLED);
        self.invalidateCaps();
        scanClientEntity(self.getId(), clientLevel);
        if (e != null || found != null) {
            LOGGER.info("[EraserMod] failed to fully remove client entity id=" + self.getId());
            ClientboundRemoveEntitiesPacket packet =
                    new ClientboundRemoveEntitiesPacket(self.getId());
            ClientPacketListener connection = mc.getConnection();
            packet.handle(connection);
            ClientEvents.erasedEntities.add(self);
        } else {
            LOGGER.info("[EraserMod] successfully removed client entity id=" + self.getId());
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static void scanClientEntity(int id, ClientLevel level) {
        if (level == null) return;
        Entity byIdOrUuid = level.getEntity(id);
        if (byIdOrUuid != null) {
            LOGGER.info("[EraserMod] found in level.getEntity id={} id={} class={}", id, byIdOrUuid.getId(), byIdOrUuid.getClass().getName());
            level.removeEntity(byIdOrUuid.getId(), Entity.RemovalReason.KILLED);
        }

        List<Entity> renderSnapshot = StreamSupport.stream(level.entitiesForRendering().spliterator(), false).collect(Collectors.toList());
        for (Entity e : renderSnapshot) {
            if (id == e.getId()) {
                LOGGER.info("[EraserMod] found in entitiesForRendering id={} id={} class={}", id, e.getId(), e.getClass().getName());
                level.removeEntity(e.getId(), Entity.RemovalReason.KILLED);
            }
        }

        try {
            TransientEntitySectionManager<?> tManager = ((ClientLevelAccessor) level).getTransientEntityManager();
            Object getterObj = tManager.getEntityGetter();
            EntityLookup<?> visible = ((LevelEntityGetterAdapterAccessor<?>) getterObj).getVisibleEntities();
            Iterable<?> all = ((EntityLookup<?>) visible).getAllEntities();
            for (Object o : all) {
                if (o instanceof Entity ent && id == ent.getId()) {
                    LOGGER.info("[EraserMod] found in visible lookup id={} class={}", id, o.getClass().getName());
                    ((EraseEntityLookupBridge<Entity>) visible).eraseEntity((Entity)o);
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("[EraserMod] visible lookup scan failed", t);
        }

        try {
            TransientEntitySectionManager<?> tManager = ((ClientLevelAccessor) level).getTransientEntityManager();
            EntitySectionStorage<?> storage = ((LevelEntityGetterAdapterAccessor<?>) tManager.getEntityGetter()).getSectionStorage();
            if (storage != null) {
                for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("[EraserMod] sectionStorage scan failed", t);
        }

        try {
            Iterator<net.minecraftforge.entity.PartEntity<?>> pit = level.getPartEntities().iterator();
            while (pit.hasNext()) {
                net.minecraftforge.entity.PartEntity<?> p = pit.next();
                if (p != null && id == p.getId()) {
                    LOGGER.info("[EraserMod] found in partEntities id={} class={}", id, p.getParent().getClass().getName());
                    pit.remove();
                }
            }
        } catch (Throwable t) {
            LOGGER.debug("[EraserMod] partEntities scan failed", t);
        }
    }

    @Unique
    private void hardRemove(Entity self, Map<Class<?>, List<Entity>> byClass) {
        List<Entity> list = byClass.get(self.getClass());
        if (list != null) {
            list.remove(self);
            return;
        }
        return;
    }
}