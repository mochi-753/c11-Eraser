package com.test.eraser.client;

import com.test.eraser.additional.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
                    if (tintIndex == 1) {
                        long time = System.currentTimeMillis();
                        double wave = (Math.sin(time / 700.0) + 1.0) / 2.0;
                        int gray = 0xCCCCCC;
                        int white = 0xFFFFFF;
                        int r = (int) (((gray >> 16) & 0xFF) * (1 - wave) + ((white >> 16) & 0xFF) * wave);
                        int g = (int) (((gray >> 8) & 0xFF) * (1 - wave) + ((white >> 8) & 0xFF) * wave);
                        int b = (int) ((gray & 0xFF) * (1 - wave) + (white & 0xFF) * wave);
                        return (r << 16) | (g << 8) | b;
                    }
                    return 0xFFFFFF;
                }, ModItems.ERASER_ITEM.get(),
                ModItems.WORLD_DESTROYER.get());

    }
}
