package com.test.eraser.additional;

import com.test.eraser.Eraser;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.Set;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Eraser.MODID);
    public static final RegistryObject<CreativeModeTab> ERASER_TAB =
            TABS.register("eraser_tab", () -> CreativeModeTab.builder()
                    .title(Component.literal("Eraser"))
                    .icon(() -> new ItemStack(ModItems.ERASER_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        ModItems.getAllItems().forEach(item -> output.accept(new ItemStack(item)));
                    })
                    .build());
    private static final Set<Item> ERASER_TAB_ITEMS = new HashSet<>();

    private static void addToTab(CreativeModeTab.Output output, Item item) {
        output.accept(item);
        ERASER_TAB_ITEMS.add(item);
    }

    public static boolean isInEraserTab(ItemStack stack) {
        return ERASER_TAB_ITEMS.contains(stack.getItem());
    }
}
