package com.test.eraser;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.test.eraser.additional.*;
import com.test.eraser.network.ModPackets;
import com.test.eraser.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
@SuppressWarnings("removal")
@Mod(Eraser.MODID)
public class Eraser {
    public static final String MODID = "eraser";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Eraser() {
        MinecraftForge.EVENT_BUS.register(this);

        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        if (ModList.get().isLoaded("ironsspellbooks")) {
            ModSpells.register(modEventBus);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModPackets.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
