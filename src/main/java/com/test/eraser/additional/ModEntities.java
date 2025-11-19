package com.test.eraser.additional;

import com.test.eraser.Eraser;
import com.test.eraser.entity.HomingArrowEntity;
import com.test.eraser.entity.Sand_Bag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Eraser.MODID);

    public static final RegistryObject<EntityType<HomingArrowEntity>> HOMING_ARROW =
            ENTITIES.register("homing_arrow",
                    () -> EntityType.Builder.<HomingArrowEntity>of(HomingArrowEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("homing_arrow"));

    public static final RegistryObject<EntityType<Sand_Bag>> SAND_BAG =
            ENTITIES.register("sand_bag",
                    () -> EntityType.Builder.<Sand_Bag>of(Sand_Bag::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("sand_bag"));
}
