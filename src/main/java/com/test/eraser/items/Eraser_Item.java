package com.test.eraser.items;

import com.test.eraser.additional.ModTiers;
import com.test.eraser.entity.HomingArrowEntity;
import com.test.eraser.logic.ILivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

public class Eraser_Item extends SwordItem {
    public Eraser_Item(Properties props) {
        super(ModTiers.ERASER_TIER, 10, -2.4F, props.stacksTo(1).fireResistant());
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (target instanceof ILivingEntity erased) {
            erased.toolinstantKill(player);
        }
        target.kill();
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                HomingArrowEntity arrow = new HomingArrowEntity(level, player);
                arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

                arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);

                level.addFreshEntity(arrow);
            }
        }
        player.swing(hand, true);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    public Component getName(ItemStack stack) {
        String text = "Eraser.";
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

        String desc = Component.translatable("item.eraser.eraser_item.desc").getString();
        var waveLine = Component.empty();
        for (int i = 0; i < desc.length(); i++) {
            char c = desc.charAt(i);
            int color = waveGrayWhiteColor(gameTime, i, 6.0);
            waveLine = waveLine.append(
                    Component.literal(String.valueOf(c))
                            .withStyle(s -> s.withColor(color))
            );
        }
        String desc2 = Component.literal("Fortune VII").getString();
        var waveLine2 = Component.empty();
        for (int i = 0; i < desc2.length(); i++) {
            char c = desc2.charAt(i);
            int color = waveGrayWhiteColor(gameTime, i, 6.0);
            waveLine2 = waveLine2.append(
                    Component.literal(String.valueOf(c))
                            .withStyle(s -> s.withColor(color))
            );
        }
        tooltip.add(1, waveLine);
        tooltip.add(2, waveLine2);
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
