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
        if (event.getConfig() == null || event.getConfig().getSpec() != Config.CLIENT_SPEC) return;
        var names = INSTANT_KILL_ITEMS.get();
        if (names == null) return;

        instantKillItems = names.stream()
                .map(name -> {
                    try {
                        return ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(name));
                    } catch (Exception ex) {
                        LOGGER.warn("Invalid item resource location in config: {}", name, ex);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}*/