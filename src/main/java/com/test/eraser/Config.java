package com.test.eraser;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*@Mod.EventBusSubscriber(modid = Eraser.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> INSTANT_KILL_ITEMS = BUILDER
            .comment("Items that can perform instant kill")
            .defineListAllowEmpty(
                    "instantKillItems",
                    List.of("eraser:eraser_item"),
                    obj -> obj instanceof String s && ForgeRegistries.ITEMS.containsKey(ResourceLocation.parse(s))
            );
    public static Set<Item> instantKillItems;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        instantKillItems = INSTANT_KILL_ITEMS.get().stream()
                .map(name -> ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(name)))
                .collect(Collectors.toSet());
    }
}*/