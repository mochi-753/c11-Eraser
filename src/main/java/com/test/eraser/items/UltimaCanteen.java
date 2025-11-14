package com.test.eraser.items;

import com.test.eraser.mixin.eraser.LivingEntityAccessor;
import com.test.eraser.utils.SynchedEntityDataUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class UltimaCanteen extends Item {
    private static final float SATURATION_TARGET = 7.0f;
    private static final int FOOD_LEVEL_TARGET = 20;

    public UltimaCanteen(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide()) {
            player.swing(hand, true);
            return InteractionResultHolder.consume(stack);
        }
        else {
            float maxHp = player.getMaxHealth();
            float healAmount = maxHp * 0.4f;
            float newHp = Math.min(player.getHealth() + healAmount, maxHp);
            player.setHealth(newHp);

            try {
                int currentFood = player.getFoodData().getFoodLevel();
                if (currentFood < FOOD_LEVEL_TARGET) {
                    int addFood = FOOD_LEVEL_TARGET - currentFood;
                    player.getFoodData().eat(addFood, SATURATION_TARGET);
                }

                float currentSat = player.getFoodData().getSaturationLevel();
                if (currentSat < SATURATION_TARGET) {
                    float addSat = SATURATION_TARGET - currentSat;
                    player.getFoodData().eat(0, addSat);
                }

            } catch (Throwable t) {
                t.printStackTrace();
            }

            world.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.getCooldowns().addCooldown(this, 70);
        }

        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    @Override
    public Component getName(ItemStack stack) {
        String text = Component.literal("Canteen").getString();
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

        String special = "Solve food problem!";
        String[] partsSpecial = special.split(" ");
        var waveLineSpecial = Component.empty();

        int gold = 0xFFD700;
        int Gold2 = 0xD4AF37;

        for (int i = 0; i < partsSpecial.length; i++) {
            double wave = 0.5 + 0.5 * Math.sin((gameTime / 6.5) + i);
            int r = (int) (((gold >> 16) & 0xFF) * wave + ((Gold2 >> 16) & 0xFF) * (1 - wave));
            int g = (int) (((gold >> 8) & 0xFF) * wave + ((Gold2 >> 8) & 0xFF) * (1 - wave));
            int b = (int) ((gold & 0xFF) * wave + (Gold2 & 0xFF) * (1 - wave));
            int blended = (r << 16) | (g << 8) | b;

            waveLineSpecial = waveLineSpecial.append(
                    Component.literal(partsSpecial[i])
                            .withStyle(s -> s.withColor(blended))
            );
            if (i < partsSpecial.length - 1) {
                waveLineSpecial = waveLineSpecial.append(Component.literal(" "));
            }
        }
        tooltip.add(1, waveLineSpecial);
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
}