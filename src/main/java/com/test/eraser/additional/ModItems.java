package com.test.eraser.additional;

import com.test.eraser.Eraser;
import com.test.eraser.items.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.stream.Collectors;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Eraser.MODID);

    public static final RegistryObject<Item> ERASER_ITEM =
            ITEMS.register("eraser_item", () -> new Eraser_Item(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WORLD_DESTROYER =
            ITEMS.register("world_destroyer", () -> new World_Destroyer_Item(new Item.Properties()));

    public static final RegistryObject<Item> NULL_INGOT =
            ITEMS.register("null_ingot", () -> new Null_Ingot_Item(new Item.Properties()));

    public static final RegistryObject<Item> SNACK_HELMET =
            ITEMS.register("snack_protect_helmet", () -> new Snack_Helmet());

    public static final RegistryObject<Item> SNACK_CHESTPLATE =
            ITEMS.register("snack_protect_chestplate", () -> new Snack_ChestPlate());

    public static final RegistryObject<Item> SNACK_LEGGINGS =
            ITEMS.register("snack_protect_leggings", () -> new Snack_Leggings());

    public static final RegistryObject<Item> SNACK_BOOTS =
            ITEMS.register("snack_protect_boots", () -> new Snack_Boots());
    /*public static final RegistryObject<Item> GAZE_DEATH_SCROLL_1 =
            ITEMS.register("gaze_death_scroll_1",
                    () -> new GazeDeathScroll_Item(
                            new Item.Properties()
                                    .stacksTo(16)
                                    .rarity(net.minecraft.world.item.Rarity.RARE),
                            1
                    )
            );
    public static final RegistryObject<Item> GAZE_DEATH_SCROLL_2 =
            ITEMS.register("gaze_death_scroll_2",
                    () -> new GazeDeathScroll_Item(
                            new Item.Properties()
                                    .stacksTo(16)
                                    .rarity(net.minecraft.world.item.Rarity.RARE),
                            2
                    )
            );
    public static final RegistryObject<Item> GAZE_DEATH_SCROLL_3 =
            ITEMS.register("gaze_death_scroll_3",
                    () -> new GazeDeathScroll_Item(
                            new Item.Properties()
                                    .stacksTo(16)
                                    .rarity(net.minecraft.world.item.Rarity.RARE),
                            3
                    )
            );*/

    public static List<Item> getAllItems() {
        return ITEMS.getEntries().stream()
                .map(RegistryObject::get)
                .collect(Collectors.toList());
    }
}