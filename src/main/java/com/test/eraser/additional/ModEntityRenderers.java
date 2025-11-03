package com.test.eraser.additional;

import com.test.eraser.entity.HomingArrowEntity;
import com.test.eraser.utils.Res;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class ModEntityRenderers {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HOMING_ARROW.get(),
                context -> new ArrowRenderer<HomingArrowEntity>(context) {
                    @Override
                    public ResourceLocation getTextureLocation(HomingArrowEntity entity) {
                        return Res.getResource("minecraft", "textures/entity/projectiles/arrow.png");
                    }
                }
        );

    }
}
