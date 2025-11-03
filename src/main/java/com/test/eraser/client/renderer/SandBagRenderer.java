package com.test.eraser.client.renderer;

import com.test.eraser.entity.Sand_Bag;
import com.test.eraser.utils.Res;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SandBagRenderer extends MobRenderer<Sand_Bag, SandBagModel> {
    private static final ResourceLocation TEXTURE =
            Res.getResource("minecraft", "textures/entity/illager/vex.png");

    public SandBagRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new SandBagModel(ctx.bakeLayer(ModelLayers.VEX)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(Sand_Bag entity) {
        return TEXTURE;
    }
}