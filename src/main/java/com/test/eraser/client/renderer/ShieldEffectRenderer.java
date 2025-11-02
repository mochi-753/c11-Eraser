package com.test.eraser.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.test.eraser.client.utils.RenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShieldEffectRenderer {
    private static final ResourceLocation NOISE_TEXTURE =
            new ResourceLocation("eraser", "textures/misc/noise_overlay.png");

    private static final List<ShieldInstance> activeShields = new ArrayList<>();

    public static void spawnShield(Vec3 pos) {
        activeShields.add(new ShieldInstance(pos, 10));
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, float partialTicks) {

        Iterator<ShieldInstance> it = activeShields.iterator();
        while (it.hasNext()) {
            ShieldInstance shield = it.next();
            shield.ticks--;

            poseStack.pushPose();
            poseStack.translate(shield.pos.x, shield.pos.y, shield.pos.z);
            System.out.println("x: " + shield.pos.x + "y: " + shield.pos.y + "y: " + shield.pos.z);
            poseStack.scale(5.5f, 5.5f, 5.5f);

            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(NOISE_TEXTURE));
            RenderUtils.drawBillboardQuad(poseStack, consumer, 1f, 1f, 1f, 0.8f);

            poseStack.popPose();

            if (shield.ticks <= 0) it.remove();
        }
    }

    private static class ShieldInstance {
        Vec3 pos;
        int ticks;

        ShieldInstance(Vec3 pos, int ticks) {
            this.pos = pos;
            this.ticks = ticks;
        }
    }
}