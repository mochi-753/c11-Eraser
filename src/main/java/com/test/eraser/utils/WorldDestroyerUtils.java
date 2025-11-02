package com.test.eraser.utils;

import com.test.eraser.Eraser;
import com.test.eraser.additional.ModItems;
import com.test.eraser.logic.DestroyBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Eraser.MODID)
public class WorldDestroyerUtils {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack held = player.getMainHandItem();
        if (held.is(ModItems.WORLD_DESTROYER.get())) {
            var pos = event.getPos();
            var level = (ServerLevel) event.getLevel();

            DestroyMode mode = DestroyMode.getMode(held);

            DestroyBlock.breakAreaWithFortune(level, player, pos, mode, held, 7);

            event.setCanceled(true);
        }
    }
}
