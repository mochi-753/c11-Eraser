package com.test.eraser.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.test.eraser.Eraser;
import com.test.eraser.additional.ModItems;
import com.test.eraser.utils.TintingVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    private static final List<RegistryObject<Item>> AFFECTED_ITEMS = List.of(
            ModItems.SNACK_HELMET,
            ModItems.SNACK_CHESTPLATE,
            ModItems.SNACK_LEGGINGS,
            ModItems.SNACK_BOOTS,
            ModItems.NULL_INGOT,
            ModItems.ERASER_ERASER
    );

    private static List<Item> getAffectedItems() {
        return AFFECTED_ITEMS.stream()
                .filter(RegistryObject::isPresent)
                .map(RegistryObject::get)
                .collect(Collectors.toList());
    }

    private static boolean shouldAffect(ItemStack stack, ItemDisplayContext ctx) {
        boolean inHand =
                ctx == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ||
                        ctx == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ||
                        ctx == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        boolean inGui = ctx == ItemDisplayContext.GUI;

        List<Item> currentAffectedItems = getAffectedItems();
        return (inHand || inGui) && (
                ModItems.getAllItems().stream().anyMatch(stack::is) ||
                        currentAffectedItems.stream().anyMatch(stack::is)
        );
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

        List<Item> currentAffectedItems = getAffectedItems();
        return (inHand || inGui) && (
                ModItems.getAllItems().stream().anyMatch(stack::is) ||
                        currentAffectedItems.stream().anyMatch(stack::is)
        );
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

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getFoilBufferDirect(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
                    shift = At.Shift.AFTER
            )
    )
    private void eraser$injectFoilBuffer(ItemStack stack, ItemDisplayContext ctx, boolean leftHand,
                                         PoseStack poseStack, MultiBufferSource buffer,
                                         int packedLight, int packedOverlay, BakedModel model,
                                         CallbackInfo ci) {
        if (shouldAffect(stack, ctx)) {
            long time = System.currentTimeMillis();
            int argb = waveGrayWhiteColor(time, 0, 700.0);
            float r = ((argb >> 16) & 0xFF) / 255f;
            float g = ((argb >> 8) & 0xFF) / 255f;
            float b = (argb & 0xFF) / 255f;
            float a = ((argb >> 24) & 0xFF) / 255f;

        }
    }

    /*@Inject(method = "render", at = @At("HEAD"))
    private void eraser$injectDynamic(ItemStack stack, ItemDisplayContext ctx, boolean leftHand,
                                      PoseStack poseStack, MultiBufferSource buffer,
                                      int packedLight, int packedOverlay, BakedModel model,
                                      CallbackInfo ci) {
        if (shouldAffect(stack, ctx)) {
            if(dynTex == null || dynLoc == null || img == null) {
                initTexture();
            }
            long time = System.currentTimeMillis();
            int color = waveGrayWhiteColor(time, 0, 700.0);
            System.out.println("alpha=" + ((color >>> 24) & 0xFF));

            img.fillRect(0, 0, img.getWidth(), img.getHeight(), color);
            dynTex.upload();

            RenderType dynType = RenderType.text(dynLoc);
            VertexConsumer vc = buffer.getBuffer(dynType);
            ((ItemRendererAccessor)(Object)this).callRenderModelLists(model, stack, packedLight, packedOverlay, poseStack, vc);
        }
    }*/

    private static DynamicTexture dynTex = null;
    private static ResourceLocation dynLoc = null;
    private static NativeImage img = null;

    private static void initTexture() {
        if (dynTex == null) {
            img = new NativeImage(16, 16, true); // 16x16 RGBA?
            dynTex = new DynamicTexture(img);
            dynLoc = Minecraft.getInstance().getTextureManager()
                    .register("eraser:item_overlay", dynTex);
        }
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
