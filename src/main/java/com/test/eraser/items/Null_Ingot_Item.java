package com.test.eraser.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class Null_Ingot_Item extends Item {
    public Null_Ingot_Item(Properties props) {
        super(props);
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

    @Override
    public Component getName(ItemStack stack) {
        String text = "? Ingot";
        var result = Component.empty();
        long time = System.currentTimeMillis() / 50;

        for (int i = 0; i < text.length(); i++) {
            int color = waveGrayWhiteColor(time, i, 5.0);
            result = result.append(Component.literal(String.valueOf(text.charAt(i)))
                    .withStyle(style -> style.withColor(color)));
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long gameTime = (level != null) ? level.getGameTime() : 0;
        String desc = Component.translatable("item.eraser.null_ingot.desc").getString();

        String[] parts = desc.split(" ");
        var waveLine = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            int color = waveGrayWhiteColor(gameTime, i, 6.5);
            waveLine = waveLine.append(
                    Component.literal(parts[i])
                            .withStyle(s -> s.withColor(color))
            );
            if (i < parts.length - 1) {
                waveLine = waveLine.append(Component.literal(" "));
            }
        }

        tooltip.add(1, waveLine);
    }
}
