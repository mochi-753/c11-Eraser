package com.test.eraser.mixin.client;

import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.mixin.eraser.ClassInstanceMultiMapAccessor;
import com.test.eraser.mixin.eraser.EntitySectionAccessor;
import com.test.eraser.mixin.eraser.LevelEntityGetterAdapterAccessor;
import com.test.eraser.mixin.eraser.TransientEntitySectionManagerAccessor;
import com.test.eraser.utils.EraseEntityLookupBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements ILivingEntity {
// "client.ClientLevelAccessor",
    @Override
    public void eraseClientEntity(LivingEntity self) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && mc.player.level() instanceof ClientLevel clientLevel) {
            TransientEntitySectionManager<Entity> tManager = ((ClientLevelAccessor) clientLevel).getTransientEntityManager();
            TransientEntitySectionManagerAccessor tAcc = (TransientEntitySectionManagerAccessor) tManager;

            long tSectionKey = SectionPos.asLong(self.blockPosition());
            EntitySection<Entity> tSection = tAcc.getSectionStorage().getSection(tSectionKey);
            if (tSection != null) {
                ((EntitySectionAccessor) tSection).getStorage().remove(self);
                ClassInstanceMultiMap<Entity> multiMap = ((EntitySectionAccessor<Entity>) tSection).getStorage();
                Map<Class<?>, List<Entity>> byClass = ((ClassInstanceMultiMapAccessor<Entity>) multiMap).getByClass();
                hardRemove(self, byClass);
            }

            EntityLookup<Entity> tVisible =
                    ((LevelEntityGetterAdapterAccessor<Entity>) tManager.getEntityGetter()).getVisibleEntities();
            ((EraseEntityLookupBridge<Entity>) tVisible).eraseEntity(self);

            clientLevel.removeEntity(self.getId(), Entity.RemovalReason.KILLED);
            Entity e = clientLevel.getEntity(self.getId());
            if (e != null) {
                System.err.println("[EraserMod] failed to fully remove client entity id=" + self.getId());
            } else {
                System.out.println("[EraserMod] successfully removed client entity id=" + self.getId());
            }
        }
    }

    private void hardRemove(Entity self, Map<Class<?>, List<Entity>> byClass) {
        List<Entity> list = byClass.get(self.getClass());
        if (list != null) {
            list.remove(self);
        }
    }
}