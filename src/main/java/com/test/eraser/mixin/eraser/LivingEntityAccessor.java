package com.test.eraser.mixin.eraser;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("lastHurtByPlayer")
    Player getLastHurtByPlayer();

    @Accessor("lastHurtByPlayer")
    void setLastHurtByPlayer(Player player);

    @Accessor("lastHurtByPlayerTime")
    int getLastHurtByPlayerTime();

    @Accessor("lastHurtByPlayerTime")
    void setLastHurtByPlayerTime(int time);

    @Accessor("DATA_HEALTH_ID")
    static EntityDataAccessor<Float> getDataHealthId() {
        throw new AssertionError();
    }

    @Invoker("dropExperience")
    void invokeDropExperience();

    @Invoker("getKillCredit")
    LivingEntity invokeGetKillCredit();

    @Invoker("dropAllDeathLoot")
    void invokeDropAllDeathLoot(DamageSource source);

    @Accessor("dead")
    boolean isDeadFlag();

    @Accessor("dead")
    void setDeadFlag(boolean dead);

}
