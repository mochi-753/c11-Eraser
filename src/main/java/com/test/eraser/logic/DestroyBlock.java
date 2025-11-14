package com.test.eraser.logic;

import com.test.eraser.client.RenderQueue;
import com.test.eraser.mixin.world_destroyer.ServerLevelAccessor;
import com.test.eraser.mixin.world_destroyer.ServerLevelMixin;
import com.test.eraser.utils.DestroyMode;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.*;
import java.util.function.Predicate;

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

        ((IServerLevel)level).forceSetBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL, true);

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

    public static void breakSameId(ServerLevel level, ServerPlayer player, BlockPos center,
                                   ItemStack tool, int fortuneLevel, boolean silk, int maxCount,
                                   Predicate<BlockState> accept) {
        BlockState originState = level.getBlockState(center);
        if (originState.isAir()) return;

        Deque<BlockPos> queue = new ArrayDeque<>();
        Set<Long> visited = new LongOpenHashSet();

        queue.add(center);
        visited.add(center.asLong());

        int destroyed = 0;

        while (!queue.isEmpty() && destroyed < maxCount) {
            BlockPos pos = queue.pollFirst();
            BlockState state = level.getBlockState(pos);

            if (state.isAir()) continue;
            if (!accept.test(state)) continue;

            doBreakBlock(level, player, pos, tool, fortuneLevel, silk);
            destroyed++;
            if (destroyed >= maxCount) break;

            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                long key = next.asLong();
                if (visited.contains(key)) continue;
                visited.add(key);
                queue.addLast(next);
            }
        }
    }

    public static void breakSameIdById(ServerLevel level, ServerPlayer player, BlockPos center,
                                       ItemStack tool, int fortuneLevel, boolean silk, int maxCount,
                                       ResourceLocation blockId) {
        Block target = BuiltInRegistries.BLOCK.get(blockId);
        if (target == null || target == Blocks.AIR) return;

        breakSameId(level, player, center, tool, fortuneLevel, silk, maxCount,
                state -> state.getBlock() == target);
    }

    public static void breakSameIdByIdNormal(ServerLevel level, ServerPlayer player, BlockPos center,
                                             ItemStack tool, ResourceLocation blockId) {
        breakSameIdById(level, player, center, tool, 0, false, 32, blockId);
    }

    public static void breakSameIdByIdFortune(ServerLevel level, ServerPlayer player, BlockPos center,
                                              ItemStack tool, int fortuneLevel, ResourceLocation blockId) {
        breakSameIdById(level, player, center, tool, fortuneLevel, false, 32, blockId);
    }

    public static void breakSameIdByIdSilk(ServerLevel level, ServerPlayer player, BlockPos center,
                                           ItemStack tool, ResourceLocation blockId) {
        breakSameIdById(level, player, center, tool, 0, true, 32, blockId);
    }
    public static void breakSameIdNormal(ServerLevel level, ServerPlayer player, BlockPos center, ItemStack tool) {
        breakSameId(level, player, center, tool, 0, false, 32,
                state -> state.getBlock() == level.getBlockState(center).getBlock());
    }

    public static void breakSameIdFortune(ServerLevel level, ServerPlayer player, BlockPos center, ItemStack tool, int fortuneLevel) {
        breakSameId(level, player, center, tool, fortuneLevel, false, 32,
                state -> state.getBlock() == level.getBlockState(center).getBlock());
    }

    public static void breakSameIdSilk(ServerLevel level, ServerPlayer player, BlockPos center, ItemStack tool) {
        breakSameId(level, player, center, tool, 0, true, 32,
                state -> state.getBlock() == level.getBlockState(center).getBlock());
    }

    public static List<BlockPos> getBreakPositions(Level level, Player player, BlockPos center, DestroyMode mode, boolean sameId, int maxCount, Predicate<BlockState> accept) {
        List<BlockPos> result = new ArrayList<>();

        if (sameId) {
            //Same ID Mode
            BlockState originState = level.getBlockState(center);
            if (originState.isAir()) return result;

            Deque<BlockPos> queue = new ArrayDeque<>();
            Set<Long> visited = new HashSet<>();

            queue.add(center);
            visited.add(center.asLong());

            int destroyed = 0;

            while (!queue.isEmpty() && destroyed < maxCount) {
                BlockPos pos = queue.pollFirst();
                BlockState state = level.getBlockState(pos);

                if (state.isAir()) continue;
                if (!accept.test(state)) continue;

                result.add(pos);
                destroyed++;
                if (destroyed >= maxCount) break;

                for (Direction dir : Direction.values()) {
                    BlockPos next = pos.relative(dir);
                    long key = next.asLong();
                    if (visited.contains(key)) continue;
                    visited.add(key);
                    queue.addLast(next);
                }
            }
        } else {
            //Area break mode
            int halfX = mode.x / 2;
            int halfY = mode.y / 2;
            int halfZ = mode.z / 2;

            BlockPos base = center.above(mode.yOffset);
            Direction facing = player.getDirection();

            for (int dx = -halfX; dx <= halfX; dx++) {
                for (int dy = -halfY; dy <= halfY; dy++) {
                    for (int dz = -halfZ; dz <= halfZ; dz++) {
                        BlockPos target;
                        switch (facing) {
                            case NORTH -> target = base.offset(dx, dy, -dz);
                            case WEST  -> target = base.offset(-dz, dy, dx);
                            case EAST  -> target = base.offset(dz, dy, dx);
                            default    -> target = base.offset(dx, dy, dz);
                        }
                        result.add(target);
                    }
                }
            }
        }

        return result;
    }

    public static void QueueRenderBreakBlock(Level level, Player player, BlockPos center, DestroyMode mode, boolean sameId, int maxCount, Predicate<BlockState> accept) {
        List<BlockPos> targets = getBreakPositions(level, player, center, mode, sameId, maxCount, accept);
        RenderQueue.clear();
        for (BlockPos pos : targets) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            RenderQueue.add(new RenderQueue.RenderEntry(pos, 0xFFFFFFFF));
        }
    }
}
