package com.test.eraser.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.test.eraser.Eraser;
import com.test.eraser.additional.SnackArmor;
import com.test.eraser.utils.Res;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class CustomArmorOverlayLayer<T extends LivingEntity, M extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    private static final ResourceLocation OVERLAY =
            Res.getResource(Eraser.MODID, "textures/misc/noise_overlay.png");

    public CustomArmorOverlayLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
                       T entity, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity instanceof Player player && SnackArmor.SnackProtector.isFullSet(player)) {
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(OVERLAY));
            this.getParentModel().renderToBuffer(poseStack, consumer, light,
                    OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 0.1f);
        }

    }
}