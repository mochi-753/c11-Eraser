package com.test.eraser.additional;

import com.test.eraser.Eraser;
import com.test.eraser.gui.BagMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.extensions.IForgeMenuType;

@Mod.EventBusSubscriber(modid = Eraser.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Eraser.MODID);

    public static final RegistryObject<MenuType<BagMenu>> BAG_MENU =
            MENUS.register("bag_menu",
                    () -> IForgeMenuType.create((windowId, inv, buf) -> new BagMenu(windowId, inv, buf))
            );
}
