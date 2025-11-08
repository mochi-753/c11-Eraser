package com.test.eraser.utils;

import com.test.eraser.Eraser;
import com.test.eraser.additional.ModItems;
import com.test.eraser.logic.DestroyBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = Eraser.MODID)
public class WorldDestroyerUtils {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(ModItems.WORLD_DESTROYER.get())) return;

        var pos = event.getPos();
        var level = (ServerLevel) event.getLevel();

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

                Predicate<BlockState> orePredicate = state -> state.is(FORGE_ORES);
                if (silk) {
                    DestroyBlock.breakSameId(level, player, pos, held, 0, true, 32, orePredicate);
                    DestroyBlock.breakBlockSilk(level, player, pos, held);
                } else {
                    DestroyBlock.breakSameId(level, player, pos, held, fortuneLevel, false, 32, orePredicate);
                    DestroyBlock.breakAreaWithFortune(level, player, pos, mode, held, fortuneLevel);
                }
            }
            default -> DestroyBlock.breakAreaWithFortune(level, player, pos, mode, held, fortuneLevel);
        }

        event.setCanceled(true);
    }
}
