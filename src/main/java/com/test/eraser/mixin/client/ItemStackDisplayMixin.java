package com.test.eraser.mixin.client;

import com.test.eraser.additional.ModItems;
import com.test.eraser.utils.DestroyMode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackDisplayMixin {

    private static MutableComponent makeWaveLine(String text, boolean grayWhite) {
        long time = System.currentTimeMillis() / 50;
        MutableComponent waveLine = Component.empty();
        for (int i = 0; i < text.length(); i++) {
            int color = grayWhite
                    ? waveGrayWhiteColor(time, i, 6.0)
                    : waveYellowGoldColor(time, i, 6.0);
            waveLine = waveLine.append(
                    Component.literal(String.valueOf(text.charAt(i)))
                            .withStyle(s -> s.withColor(color))
            );
        }
        return waveLine;
    }

    private static Component buildInfinityLine(Component attributeName) {
        long time = System.currentTimeMillis() / 50;
        String text = " Infinity " + attributeName.getString();
        MutableComponent waveLine = Component.empty();
        for (int j = 0; j < text.length(); j++) {
            int color = waveGrayWhiteColor(time, j, 6.0);
            waveLine = waveLine.append(
                    Component.literal(String.valueOf(text.charAt(j)))
                            .withStyle(s -> s.withColor(color))
            );
        }
        return Component.literal("").append(waveLine);
    }

    private static int waveYellowGoldColor(long time, int index, double speed) {
        double wave = (Math.sin((time / speed) + index) + 1.0) / 2.0;
        int yellow = 0xFFFF55;
        int gold = 0xFFAA00;
        int r = (int) (((yellow >> 16) & 0xFF) * (1 - wave) + ((gold >> 16) & 0xFF) * wave);
        int g = (int) (((yellow >> 8) & 0xFF) * (1 - wave) + ((gold >> 8) & 0xFF) * wave);
        int b = (int) ((yellow & 0xFF) * (1 - wave) + (gold & 0xFF) * wave);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    private static int waveGrayWhiteColor(long time, int index, double speed) {
        double wave = (Math.sin((time / speed) + index) + 1.0) / 2.0;
        int gray = 0xAAAAAA;
        int white = 0xFFFFFF;
        int r = (int) (((gray >> 16) & 0xFF) * (1 - wave) + ((white >> 16) & 0xFF) * wave);
        int g = (int) (((gray >> 8) & 0xFF) * (1 - wave) + ((white >> 8) & 0xFF) * wave);
        int b = (int) ((gray & 0xFF) * (1 - wave) + (white & 0xFF) * wave);
        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void injectTooltip(@Nullable Player player, TooltipFlag flag, CallbackInfoReturnable<List<Component>> cir) {
        if (player == null) return;
        List<Component> tooltip = cir.getReturnValue();
        ItemStack stack = (ItemStack) (Object) this;

        boolean isEraserOrWorld = stack.getItem() == ModItems.ERASER_ITEM.get() || stack.getItem() == ModItems.WORLD_DESTROYER.get();
        boolean isSnackProtector = stack.getItem() == ModItems.SNACK_BOOTS.get() || stack.getItem() == ModItems.SNACK_LEGGINGS.get() || stack.getItem() == ModItems.SNACK_CHESTPLATE.get() || stack.getItem() == ModItems.SNACK_HELMET.get();
        //my brain is disabled and suck and shit and hate
        if (isEraserOrWorld || isSnackProtector) {
            String attackKeyStr = Component.translatable("attribute.name.generic.attack_damage").getString();
            String armorKeyStr = Component.translatable("attribute.name.generic.armor").getString();
            String toughnessKeyStr = Component.translatable("attribute.name.generic.armor_toughness").getString();

            for (int i = 0; i < tooltip.size(); i++) {
                Component line = tooltip.get(i);
                String lineStr = line.getString();

                if (lineStr.contains(attackKeyStr)) {
                    Component attrComp = Component.translatable("attribute.name.generic.attack_damage");
                    tooltip.set(i, buildInfinityLine(attrComp));
                } else if (lineStr.contains(armorKeyStr)) {
                    Component attrComp = Component.translatable("attribute.name.generic.armor");
                    tooltip.set(i, buildInfinityLine(attrComp));
                } else if (lineStr.contains(toughnessKeyStr)) {
                    Component attrComp = Component.translatable("attribute.name.generic.armor_toughness");
                    tooltip.set(i, buildInfinityLine(attrComp));
                }
            }
            if (stack.getItem() == ModItems.ERASER_ITEM.get()) {
                tooltip.add(Component.translatable("item.erasers.use")
                        .withStyle(ChatFormatting.GRAY));

                String normalText = " Shot Homing Arrow";
                tooltip.add(makeWaveLine(normalText, true));

                tooltip.add(Component.translatable("item.erasers.sneak_use")
                        .withStyle(ChatFormatting.DARK_GRAY));

                String sneakText = " RangeAttack";
                tooltip.add(makeWaveLine(sneakText, true));
            }

            if (stack.getItem() == ModItems.WORLD_DESTROYER.get()) {
                tooltip.add(Component.translatable("item.erasers.use")
                        .withStyle(ChatFormatting.GRAY));

                String normalText = " Get Cookie x1";
                tooltip.add(makeWaveLine(normalText, false));

                tooltip.add(Component.translatable("item.erasers.sneak_use")
                        .withStyle(ChatFormatting.DARK_GRAY));

                String sneakText = " Get Cookie x64";
                tooltip.add(makeWaveLine(sneakText, false));
            }

            cir.setReturnValue(tooltip);
        }
    }

    private boolean applycolorname(ItemStack stack) {//hate my brain
        return stack.getItem() == ModItems.ERASER_ITEM.get() || stack.getItem() == ModItems.WORLD_DESTROYER.get() || stack.getItem() == ModItems.SNACK_BOOTS.get() || stack.getItem() == ModItems.SNACK_LEGGINGS.get() || stack.getItem() == ModItems.SNACK_CHESTPLATE.get() || stack.getItem() == ModItems.SNACK_HELMET.get();
    }

    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void injectName(CallbackInfoReturnable<Component> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (applycolorname(stack)) {
            String text = cir.getReturnValue().getString(); //after rename
            long time = System.currentTimeMillis() / 50;
            if(stack.getItem() == ModItems.WORLD_DESTROYER.get()) {
                text += " Mode:[";
                text += DestroyMode.getMode(stack);
                text += "]";
                if(DestroyMode.isSilkTouchEnabled(stack)) {
                    text += " [SilkTouch Enabled]";
                }
            }
            MutableComponent waveLine = Component.empty();
            for (int i = 0; i < text.length(); i++) {
                int color = waveGrayWhiteColor(time, i, 6.0);
                waveLine = waveLine.append(
                        Component.literal(String.valueOf(text.charAt(i)))
                                .withStyle(s -> s.withColor(color))
                );
            }

            cir.setReturnValue(waveLine);
        }
    }
}

