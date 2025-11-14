package com.test.eraser;

import com.test.eraser.utils.Res;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Eraser.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue FORCE_DIE = BUILDER
            .comment("If true, entities will be force-killed If false, normal die() is used.")
            .define("forceDie", true);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NORMAL_DIE_ENTITY_IDS =
            BUILDER.comment("Entities that should use normal die() instead of forced death")
                    .defineListAllowEmpty(
                            "normalDieEntities",
                            List.of("minecraft:ender_dragon", "draconicevolution:draconic_guardian"),
                            obj -> obj instanceof String s && ResourceLocation.tryParse(s) != null
                    );

    public static final ForgeConfigSpec.BooleanValue CHENGEMODE_MESSAGE = BUILDER
            .comment("If enabled ClientMessage will be displayed when changing World Destroyer's mode.")
            .define("client_message", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private static Set<ResourceLocation> normalDieEntities = Set.of();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig() == null || event.getConfig().getSpec() != Config.SPEC) return;

        var names = NORMAL_DIE_ENTITY_IDS.get();
        if (names != null) {
            normalDieEntities = names.stream()
                    .map(ResourceLocation::tryParse)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
    }

    public static boolean isNormalDieEntity(Entity entity) {
        return normalDieEntities.contains(EntityType.getKey(entity.getType()));
    }
}