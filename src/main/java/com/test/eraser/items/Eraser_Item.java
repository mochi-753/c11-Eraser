package com.test.eraser.items;

import com.test.eraser.additional.ModTiers;
import com.test.eraser.entity.HomingArrowEntity;
import com.test.eraser.logic.DestroyBlock;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.utils.DestroyMode;
import io.redspace.ironsspellbooks.player.ClientPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static com.test.eraser.utils.Eraser_Utils.killIfParentFound;

public class Eraser_Item extends SwordItem {
    public Eraser_Item(Properties props) {
        super(ModTiers.ERASER_TIER, 10, 3.F, props.stacksTo(1).fireResistant());
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
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        killIfParentFound(target, player, 32);
        if(!(target instanceof LivingEntity))target.kill();
        return false;
    }

    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if(entity instanceof ServerPlayer player) {
            if (player.isSleeping()) {
                return false;
            }
            /*Predicate<BlockState> LogPredicate = state ->
                    state.is(BlockTags.LOGS);
            if(getPlayerLookingAt(player,5) != null && !player.level().getBlockState(getPlayerLookingAt(player,5).getBlockPos()).isAir()) {
                DestroyBlock.breakSameId((ServerLevel) player.level(), (ServerPlayer) player, getPlayerLookingAt(player,5).getBlockPos(), player.getMainHandItem(), 7, false, 7, LogPredicate);
                DestroyBlock.breakAreaWithFortune((ServerLevel) player.level(), player, getPlayerLookingAt(player,5).getBlockPos(), DestroyMode.NORMAL, player.getMainHandItem(), 7);
            }*/
            HitResult hitResult = player.pick(5, 1.0f, true);

            if(!player.level().getBlockState(getPlayerLookingAt(player,7).getBlockPos()).isAir() || hitResult.getType() == HitResult.Type.ENTITY) return false;
            List<Entity> entities = findEntitiesInCone(player, 3.5, 45.0);

            for (Entity ent : entities) {
                if (ent instanceof LivingEntity living) {
                    killIfParentFound(living, player, 32);
                }
            }
            player.sweepAttack();
            return true;
        }
        return false;
    }

    private List<Entity> findEntitiesInCone(Player player, double radius, double angle) {
        Level level = player.level();
        return level.getEntities(player, player.getBoundingBox().inflate(radius), entity -> {
            if (entity == player) return false;

            double distanceSq = player.distanceToSqr(entity);
            if (distanceSq > radius * radius) return false;

            Vec3 playerDir = player.getLookAngle().normalize();
            Vec3 toEntity = new Vec3(
                    entity.getX() - player.getX(),
                    entity.getY() - player.getY(),
                    entity.getZ() - player.getZ()
            );

            double dotProduct = playerDir.dot(toEntity);
            dotProduct = Math.max(-1.0, Math.min(1.0, dotProduct));
            double angleBetween = Math.acos(dotProduct) * 180 / Math.PI;

            return angleBetween <= angle;
        });
    }

    public static BlockHitResult getPlayerLookingAt(Player player, int reach) {
        Level level = player.level();

        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle().scale(reach);
        Vec3 endPosition = eyePosition.add(lookVector);

        ClipContext context = new ClipContext(
                eyePosition,
                endPosition,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                player
        );

        return level.clip(context);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return false;
    }

    /*@Override
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
    }*/

    /*@Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }*/

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
}
