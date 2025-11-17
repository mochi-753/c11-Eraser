package com.test.eraser.items;

import com.test.eraser.additional.ModItems;
import com.test.eraser.additional.ModTiers;
import com.test.eraser.logic.DestroyBlock;
import com.test.eraser.utils.DestroyMode;
import com.test.eraser.utils.Res;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static com.test.eraser.utils.Eraser_Utils.killIfParentFound;

public class World_Destroyer_Item extends PickaxeItem {
    public World_Destroyer_Item(Properties props) {
        super(ModTiers.WORLD_DESTROYER_TIER, 1, 3.F, props.stacksTo(1).fireResistant());
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
        String text = Component.translatable("item.eraser.world_destroyer").getString();
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

        String special = "Snack is World";
        String[] partsSpecial = special.split(" ");
        var waveLineSpecial = Component.empty();

        int gold = 0xFFD700;
        int titaniumGold = 0xD4AF37;

        for (int i = 0; i < partsSpecial.length; i++) {
            double wave = 0.5 + 0.5 * Math.sin((gameTime / 6.5) + i);
            int r = (int) (((gold >> 16) & 0xFF) * wave + ((titaniumGold >> 16) & 0xFF) * (1 - wave));
            int g = (int) (((gold >> 8) & 0xFF) * wave + ((titaniumGold >> 8) & 0xFF) * (1 - wave));
            int b = (int) ((gold & 0xFF) * wave + (titaniumGold & 0xFF) * (1 - wave));
            int blended = (r << 16) | (g << 8) | b;

            waveLineSpecial = waveLineSpecial.append(
                    Component.literal(partsSpecial[i])
                            .withStyle(s -> s.withColor(blended))
            );
            if (i < partsSpecial.length - 1) {
                waveLineSpecial = waveLineSpecial.append(Component.literal(" "));
            }
        }

        String desc = Component.translatable("item.eraser.world_destroyer.desc").getString();
        String[] parts = desc.split(" ");
        var waveLineNormal = Component.empty();

        for (int i = 0; i < parts.length; i++) {
            int color = waveGrayWhiteColor(gameTime, i, 6.5);
            waveLineNormal = waveLineNormal.append(
                    Component.literal(parts[i])
                            .withStyle(s -> s.withColor(color))
            );
            if (i < parts.length - 1) {
                waveLineNormal = waveLineNormal.append(Component.literal(" "));
            }
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
        tooltip.add(1, waveLineNormal);
        tooltip.add(2, waveLineSpecial);
        tooltip.add(3, waveLine2);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        killIfParentFound(target, player, 32);
        if(!(target instanceof LivingEntity))target.kill();
        return false;
    }

    /*public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) return false;
        if (player.isSleeping()) {
            return false;
        }
        ItemStack held = player.getMainHandItem();

        ServerLevel level = (ServerLevel) entity.level();
        BlockPos pos = getPlayerLookingAt(player).getBlockPos();
        DestroyMode mode = DestroyMode.getMode(held);

        int fortuneLevel = 7;
        boolean silk = DestroyMode.isSilkTouchEnabled(held);
        switch (mode) {
            case SAME_ID -> {
                var originBlockId = level.getBlockState(pos).getBlock()
                        .builtInRegistryHolder().key().location();

                if (silk) {
                    DestroyBlock.breakSameIdByIdSilk(level, player, pos, held, originBlockId);
                } else if (fortuneLevel > 0) {
                    DestroyBlock.breakSameIdByIdFortune(level, player, pos, held, fortuneLevel, originBlockId);
                } else {
                    DestroyBlock.breakSameIdByIdNormal(level, player, pos, held, originBlockId);
                }
            }
            case SAME_ID_ORE -> {
                TagKey<Block> FORGE_ORES = BlockTags.create(Res.getResource("forge", "ores"));
                Predicate<BlockState> oreOrLogPredicate = state ->
                        state.is(FORGE_ORES) || state.is(BlockTags.LOGS);

                if (silk) {
                    DestroyBlock.breakSameId(level, player, pos, held, 0, true, 32, oreOrLogPredicate);
                    DestroyBlock.breakBlockSilk(level, player, pos, held);
                } else {
                    DestroyBlock.breakSameId(level, player, pos, held, fortuneLevel, false, 32, oreOrLogPredicate);
                    DestroyBlock.breakAreaWithFortune(level, player, pos, mode, held, fortuneLevel);
                }
            }
            default -> DestroyBlock.breakAreaWithFortune(level, player, pos, mode, held, fortuneLevel);
        }

        return true;
    }*/

    public static BlockHitResult getPlayerLookingAt(Player player) {
        Level level = player.level();
        double reachDistance = 5.0;

        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle().scale(reachDistance);
        Vec3 endPosition = eyePosition.add(lookVector);

        ClipContext context = new ClipContext(
                eyePosition,
                endPosition,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player
        );

        return level.clip(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ItemStack cookie = new ItemStack(Items.COOKIE, 1);
            if (player.isShiftKeyDown()) {
                cookie = new ItemStack(Items.COOKIE, 64);
            }
            if (!player.addItem(cookie)) {
                player.drop(cookie, false);
            }
        }

        player.getCooldowns().addCooldown(this, 20);//1sec
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
