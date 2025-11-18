package com.test.eraser;

import com.mojang.logging.LogUtils;
import com.test.eraser.additional.*;
import com.test.eraser.network.ModPackets;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import static com.test.eraser.utils.Deets.*;

@SuppressWarnings("removal")
@Mod(Eraser.MODID)
public class Eraser {
    public static final String MODID = "c11eraser";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Eraser() {
        MinecraftForge.EVENT_BUS.register(this);

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModItems.ITEMS.register(modEventBus);
        ModItems.ADDON_ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        require(IRONS_SPELLBOOKS).run(() -> {
            ModSpells.register(modEventBus);
        });
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModPackets.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
