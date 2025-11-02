package com.test.eraser.client;

import com.test.eraser.Eraser;
import com.test.eraser.additional.ModEntities;
import com.test.eraser.additional.ModKeyBindings;
import com.test.eraser.client.renderer.SandBagRenderer;
import com.test.eraser.entity.HomingArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Eraser.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModRegister {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOMING_ARROW.get(),
                context -> new ArrowRenderer<HomingArrowEntity>(context) {
                    @Override
                    public ResourceLocation getTextureLocation(HomingArrowEntity entity) {
                        return new ResourceLocation("minecraft", "textures/entity/projectiles/arrow.png");
                    }
                });

        event.registerEntityRenderer(ModEntities.SAND_BAG.get(), SandBagRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ModKeyBindings.init();
        event.register(ModKeyBindings.TOGGLE_RANGE);
    }


}
