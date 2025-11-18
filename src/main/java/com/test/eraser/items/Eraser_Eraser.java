package com.test.eraser.items;

import com.mochi_753.eraser.EraserConfig;
import com.test.eraser.additional.ModTiers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Properties;

import static com.test.eraser.utils.Eraser_Utils.killIfParentFound;

public class Eraser_Eraser extends Item {
    public Eraser_Eraser(Properties props) {
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
        String text = Component.translatable("item.eraser.eraser_eraser").getString();
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
    public @NotNull InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide() && !target.level().isClientSide()) {
            playSound(target, player.level());
            killIfParentFound(target, player, 16);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            if (player.isCrouching()) {
                double eraseRadius = EraserConfig.COMMON.eraseRadius.get();
                List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(eraseRadius), p -> p != player);
                targets.forEach((target) -> killIfParentFound(target, player, 16));
                player.getItemInHand(hand).hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return EraserConfig.COMMON.eraserDurability.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long gameTime = (level != null) ? level.getGameTime() : 0;
        String desc = Component.translatable("item.eraser.eraser_eraser.desc").getString();

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

    public static void playSound(LivingEntity target, Level level) {
        if (target.level().isClientSide() || level.isClientSide()) return;

        level.playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                1.0F, 1.0F);
    }
}
