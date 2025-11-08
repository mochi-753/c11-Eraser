package com.test.eraser.logic;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface ILivingEntity {
    boolean isErased();

    void setErased(boolean erased);

    void instantKill(Player attacker);

    void instantKill();

    void forceErase();

    boolean wasFullset();//SnackProtector

    void setwasFullset(boolean fullset);

    <T> boolean removeEntity(T target, Map<Class<?>, List<T>> storage);
}
