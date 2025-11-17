package com.test.eraser.utils;

import com.test.eraser.Eraser;
import com.test.eraser.additional.ModItems;
import com.test.eraser.logic.DestroyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Predicate;

public class WorldDestroyerUtils {

    public static boolean destroyblock(ItemStack stack, ServerPlayer player) {
        if (player.isSleeping()) {
            return false;
        }
        ItemStack held = player.getMainHandItem();

        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = getPlayerLookingAt(player, 5).getBlockPos();
        DestroyMode mode = DestroyMode.getMode(held);

        int fortuneLevel = 7;
        boolean silk = DestroyMode.isSilkTouchEnabled(held);
        if (stack.getItem() == ModItems.WORLD_DESTROYER.get()) {
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
        }
        else if(stack.getItem() == ModItems.ERASER_ITEM.get()){
            Predicate<BlockState> LogPredicate = state ->
                    state.is(BlockTags.LOGS);
            if(getPlayerLookingAt(player,5) != null && !player.level().getBlockState(getPlayerLookingAt(player,5).getBlockPos()).isAir()) {
                DestroyBlock.breakSameId((ServerLevel) player.level(), player, getPlayerLookingAt(player,5).getBlockPos(), player.getMainHandItem(), 7, false, 7, LogPredicate);
                DestroyBlock.breakAreaWithFortune((ServerLevel) player.level(), player, getPlayerLookingAt(player,5).getBlockPos(), DestroyMode.NORMAL, player.getMainHandItem(), 7);
            }
        }
        return true;
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


}
