package com.test.eraser.entity;

import com.test.eraser.entity.Utils.RandomFlyGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class Sand_Bag extends FlyingMob implements RangedAttackMob {

    private final ServerBossEvent bossEvent =
            new ServerBossEvent(Component.literal("Sand Bag"),
                    ServerBossEvent.BossBarColor.PURPLE,
                    ServerBossEvent.BossBarOverlay.PROGRESS);


    public Sand_Bag(EntityType<? extends Sand_Bag> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 20, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 9999999.0D)
                .add(Attributes.ATTACK_DAMAGE, 9999.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.5D)
                .add(Attributes.FLYING_SPEED, 200.0D)
                .add(Attributes.ARMOR, 999.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 999.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RangedAttackGoal(this, 1.0D, 10, 32.0F));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(2, new RandomFlyGoal(this));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        double dist = this.distanceTo(target);

        /*int shots = (dist < 16.0D) ? 2 : 1;
        if (this.getRandom().nextInt(5) == 0) {
            for (int i = 0; i < shots; i++) {
                HomingArrowEntity arrow = new HomingArrowEntity(this.level(), this);

                double dx = target.getX() - this.getX();
                double dy = target.getY(0.333D) - arrow.getY();
                double dz = target.getZ() - this.getZ();

                arrow.shoot(dx, dy, dz, 2.0F, 0.0F);
                this.level().addFreshEntity(arrow);
            }
        }*/
    }


    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1));
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 3));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
            living.setHealth(0);
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = findNearestTarget(64.0D);
        if (target != null) {
            this.performRangedAttack(target, 1.0F);
        }

        Player nearest = this.level().getNearestPlayer(this, 128.0D);
        if (nearest != null) {
            double dist = this.distanceTo(nearest);

            double desiredMin = 32.0D;
            double desiredMax = 64.0D;

            if (dist < desiredMin) {
                Vec3 away = this.position().subtract(nearest.position()).normalize().scale(1.5D);
                this.getMoveControl().setWantedPosition(
                        this.getX() + away.x,
                        this.getY() + away.y,
                        this.getZ() + away.z,
                        10.2D
                );
            } else if (dist > desiredMax) {
                Vec3 toward = nearest.position().subtract(this.position()).normalize().scale(1.5D);
                this.getMoveControl().setWantedPosition(
                        this.getX() + toward.x,
                        this.getY() + toward.y,
                        this.getZ() + toward.z,
                        10.2D
                );
            }
        }

    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Nullable
    private LivingEntity findNearestTarget(double radius) {
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                e -> e.isAlive() && e != this
        );

        return candidates.stream()
                .filter(e -> e instanceof Player || e instanceof Monster)
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .orElse(null);
    }


}
