package com.test.eraser.additional;

import com.test.eraser.Eraser;
import com.test.eraser.utils.Res;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import com.mochi_753.eraser.register.EraserTabs;
import static com.mojang.text2speech.Narrator.LOGGER;

@Mod.EventBusSubscriber(modid = Eraser.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MochiEraserAddon {

    @SubscribeEvent
    public static void addItemsToTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceLocation eraserTabId = Res.getResource("eraser", "eraser_tab");
        if(ModItems.ERASER_ERASER.get() == null || eraserTabId == null) {
            LOGGER.info(":(");
            return;
        }
        if (event.getTabKey().location().equals(eraserTabId)) {
            event.accept(ModItems.ERASER_ERASER.get());
        }
    }
}