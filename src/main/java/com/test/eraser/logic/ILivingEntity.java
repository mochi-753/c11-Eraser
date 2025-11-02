package com.test.eraser.logic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

public interface ILivingEntity {
    boolean isErased();
    void setErased(boolean erased);

    void instantKill(Player attacker, @Nullable int moredrop);

    void instantKill();

    void toolinstantKill();

    void toolinstantKill(Player player);

    void forceErase();

    boolean wasFullset();//SnackProtector
    void setwassFullset(boolean fullset);
}
