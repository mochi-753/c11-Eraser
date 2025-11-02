package com.test.eraser.additional;

import com.test.eraser.Eraser;
import com.test.eraser.entity.Sand_Bag;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Eraser.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SAND_BAG.get(), Sand_Bag.createAttributes().build());
    }
}
