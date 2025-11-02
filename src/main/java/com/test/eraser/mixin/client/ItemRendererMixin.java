package com.test.eraser.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.test.eraser.additional.ModItems;
import com.test.eraser.utils.TintingVertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    private static final List<RegistryObject<Item>> AFFECTED_ITEMS = List.of(
            ModItems.SNACK_HELMET,
            ModItems.SNACK_CHESTPLATE,
            ModItems.SNACK_LEGGINGS,
            ModItems.SNACK_BOOTS,
            ModItems.NULL_INGOT
    );

    private static boolean shouldAffect(ItemStack stack, ItemDisplayContext ctx) {
        boolean inHand =
                ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                        ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        boolean inGui = ctx == ItemDisplayContext.GUI;

        return (inHand || inGui) && AFFECTED_ITEMS.stream()
                .map(RegistryObject::get)
                .anyMatch(stack::is);
    }

    private static boolean isHeldContext(ItemDisplayContext ctx) {
        return ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    private static boolean shouldRotate(ItemStack stack, ItemDisplayContext ctx) {
        boolean inHand =
                ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                        ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        boolean inGui = ctx == ItemDisplayContext.GUI;

        return (inHand || inGui) && ModItems.getAllItems().stream()
                .anyMatch(stack::is);
    }

    private static int waveGrayWhiteColor(long time, int index, double speed) {
        double wave = (Math.sin((time / speed) + index) + 1.0) / 2.0;
        int gray = 0xCCCCCC;
        int white = 0xFFFFFF;
        int r = (int) (((gray >> 16) & 0xFF) * (1 - wave) + ((white >> 16) & 0xFF) * wave);
        int g = (int) (((gray >> 8) & 0xFF) * (1 - wave) + ((white >> 8) & 0xFF) * wave);
        int b = (int) ((gray & 0xFF) * (1 - wave) + (white & 0xFF) * wave);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            )
    )
    private VertexConsumer eraser$redirectFoilBufferDirect(MultiBufferSource buffer, RenderType type, boolean p1, boolean p2,
                                                           ItemStack stack, ItemDisplayContext ctx, boolean leftHand,
                                                           PoseStack poseStack, MultiBufferSource buf, int light, int overlay, BakedModel model) {
        VertexConsumer base = ItemRenderer.getFoilBufferDirect(buffer, type, p1, p2);

        if (shouldAffect(stack, ctx)) {
            long time = System.currentTimeMillis();
            int argb = waveGrayWhiteColor(time, 0, 700.0);
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >> 8) & 0xFF) / 255f;
            float b = (argb & 0xFF) / 255f;
            float a = ((argb >> 24) & 0xFF) / 255f;
            return new TintingVertexConsumer(base, r, g, b, a);
        }
        return base;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void eraser$rotateInGui(ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                    PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                    int packedOverlay, BakedModel model, CallbackInfo ci) {
        if (context == ItemDisplayContext.GUI && shouldRotate(stack, context)) {
            long time = System.currentTimeMillis();
            float angle = (float) (Math.sin(time / 500.0) * 0.4);

            poseStack.pushPose();
            poseStack.translate(8.0F, 8.0F, 100.0F);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angle));
            poseStack.translate(-8.0F, -8.0F, -100.0F);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void eraser$popGui(ItemStack stack, ItemDisplayContext context, boolean leftHand,
                               PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                               int packedOverlay, BakedModel model, CallbackInfo ci) {
        if (context == ItemDisplayContext.GUI && shouldRotate(stack, context)) {
            poseStack.popPose();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void eraser$rotateHeld(ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                   PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                   int packedOverlay, BakedModel model, CallbackInfo ci) {
        if (isHeldContext(context) && shouldRotate(stack, context)) {
            long time = System.currentTimeMillis();
            float angle = (float) (Math.sin(time / 500.0) * 10.0);

            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void eraser$popHeld(ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                int packedOverlay, BakedModel model, CallbackInfo ci) {
        if (isHeldContext(context) && shouldRotate(stack, context)) {
            poseStack.popPose();
        }
    }

}
