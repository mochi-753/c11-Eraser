package com.test.eraser.logic;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ILivingEntity {
    boolean isErased();

    void setErased(boolean erased);

    void instantKill(Player attacker);

    void instantKill();

    void forceErase();

    boolean wasFullset();//SnackProtector

    void setwasFullset(boolean fullset);

    void markErased(UUID uuid);

    void unmarkErased(UUID uuid);

    boolean isErased(UUID uuid);

    // "client.ClientLevelAccessor",
    void eraseClientEntity();


}
