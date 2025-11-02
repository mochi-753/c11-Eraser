package com.test.eraser.logic;

import com.test.eraser.utils.DestroyMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.List;

public class DestroyBlock {

    public static void breakBlockNormal(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool) {
        doBreakBlock(level, player, pos, tool, 0, false);
    }

    public static void breakBlockWithFortune(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool, int fortuneLevel) {
        doBreakBlock(level, player, pos, tool, fortuneLevel, false);
    }

    public static void breakBlockSilk(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool) {
        doBreakBlock(level, player, pos, tool, 0, true);
    }

    private static void doBreakBlock(ServerLevel level, ServerPlayer player,
                                     BlockPos pos, ItemStack tool, int fortuneLevel, boolean silk) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return;

        ItemStack usedTool = tool.copy();
        if (silk) {
            usedTool.enchant(Enchantments.SILK_TOUCH, 1);
        } else if (fortuneLevel > 0) {
            usedTool.enchant(Enchantments.BLOCK_FORTUNE, fortuneLevel);
        }

        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.TOOL, usedTool)
                .withParameter(LootContextParams.ORIGIN, pos.getCenter())
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player);

        List<ItemStack> drops = state.getDrops(builder);
        if (drops.isEmpty()) {
            Item item = state.getBlock().asItem();
            if (item != Items.AIR) {
                drops = List.of(new ItemStack(item));
            }
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        for (ItemStack drop : drops) {
            ItemEntity entity = new ItemEntity(level,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    drop);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
    }

    public static void breakAreaNormal(ServerLevel level, ServerPlayer player,
                                       BlockPos center, DestroyMode mode, ItemStack tool) {
        breakArea(level, player, center, mode, tool, 0);
    }

    public static void breakAreaWithFortune(ServerLevel level, ServerPlayer player,
                                            BlockPos center, DestroyMode mode, ItemStack tool, int fortuneLevel) {
        breakArea(level, player, center, mode, tool, fortuneLevel);
    }

    public static void breakArea(ServerLevel level, ServerPlayer player, BlockPos center,
                                 DestroyMode mode, ItemStack tool, int fortuneLevel) {
        int halfX = mode.x / 2;
        int halfY = mode.y / 2;
        int halfZ = mode.z / 2;

        BlockPos base = center.above(mode.yOffset);
        Direction facing = player.getDirection();

        boolean silk = DestroyMode.isSilkTouchEnabled(tool);

        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dy = -halfY; dy <= halfY; dy++) {
                for (int dz = -halfZ; dz <= halfZ; dz++) {
                    BlockPos target;
                    switch (facing) {
                        case NORTH -> target = base.offset(dx, dy, -dz);
                        case WEST -> target = base.offset(-dz, dy, dx);
                        case EAST -> target = base.offset(dz, dy, dx);
                        default -> target = base.offset(dx, dy, dz);
                    }

                    doBreakBlock(level, player, target, tool, fortuneLevel, silk);
                }
            }
        }
    }

}
