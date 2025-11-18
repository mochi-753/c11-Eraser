package com.test.eraser.entity;

import com.test.eraser.additional.ModEntities;
import com.test.eraser.logic.ILivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class HomingArrowEntity extends AbstractArrow {//90% ChatGPT Lawl   if(!mybrain.know(HomingArrowEntity)) ChatGPTAPI.GenerateResponce("pls Create HomingArrowEntity Class.");

    private static final double SEARCH_RADIUS = 32.0;
    private static final double MAX_ANGLE_DEGREES = 25.0; //Homing Fov
    private static final double MAX_ANGLE_COS = Math.cos(Math.toRadians(MAX_ANGLE_DEGREES));
    private LivingEntity homingTarget;

    public HomingArrowEntity(EntityType<? extends HomingArrowEntity> type, Level level) {
        super(type, level);
    }

    public HomingArrowEntity(Level level, LivingEntity shooter) {
        super(ModEntities.HOMING_ARROW.get(), shooter, level);
    }

    private static boolean hasLineOfSight(Player player, Entity target) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = target.getBoundingBox().getCenter();

        ClipContext context = new ClipContext(
                eyePos,
                targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        );

        BlockHitResult result = player.level().clip(context);

        return result.getType() == HitResult.Type.MISS || result.getBlockPos().distToCenterSqr(eyePos) >= eyePos.distanceToSqr(targetPos);
    }

    private static double getAngleToTarget(Player player, Entity target) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(player.getEyePosition()).normalize();

        double dot = look.dot(toTarget);
        return Math.acos(dot);
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.inGround || this.tickCount > 80) {
            this.remove(RemovalReason.KILLED);
            return;
        }
        this.setBaseDamage(0);
        Entity owner = this.getOwner();
        LivingEntity livingOwner = (owner instanceof LivingEntity) ? (LivingEntity) owner : null;

        if (homingTarget != null && (!homingTarget.isAlive() || this.distanceToSqr(homingTarget) > SEARCH_RADIUS * SEARCH_RADIUS)) {
            homingTarget = null;
        }

        if (homingTarget != null) {
            applyHomingTowards(homingTarget);
            return;
        }

        if (livingOwner != null) {
            List<LivingEntity> candidates = level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(SEARCH_RADIUS),
                    e -> e.isAlive() && e != livingOwner && e != (Entity) this
            );

            LivingEntity found;

            if (livingOwner instanceof Player player) {
                found = candidates.stream()
                        .filter(e -> hasLineOfSight(player, e))
                        .filter(e -> isWithinViewAngle(player, e))
                        .min(Comparator.comparingDouble(e -> getAngleToTarget(player, e)))
                        .orElse(null);
            } else {
                found = candidates.stream()
                        .min(Comparator.comparingDouble(this::distanceToSqr))
                        .orElse(null);
            }

            if (found != null) {
                homingTarget = found;
                applyHomingTowards(homingTarget);
            }
        }
    }

    private boolean isWithinViewAngle(Player player, LivingEntity target) {
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle().normalize();
        Vec3 toTarget = target.position()
                .add(0, target.getBbHeight() * 0.5, 0)
                .subtract(eyePos)
                .normalize();

        double cos = lookVec.dot(toTarget);
        return cos >= MAX_ANGLE_COS;
    }

    private boolean isValidTarget(Player owner, LivingEntity target) {
        if (target == null) return false;
        if (!target.isAlive()) return false;
        double maxDistSq = SEARCH_RADIUS * SEARCH_RADIUS;
        if (this.position().distanceToSqr(target.position()) > maxDistSq) return false;
        if (!hasLineOfSight(owner, target)) return false;
        return true;
    }

    private void applyHomingTowards(LivingEntity target) {
        if (target == null) return;
        Entity owner = this.getOwner();
        if (owner != null && target.getUUID().equals(owner.getUUID())) return;

        Vec3 toTarget = target.position()
                .add(0, target.getBbHeight() * 0.5, 0)
                .subtract(this.position());

        double distSq = toTarget.lengthSqr();
        if (distSq < 1e-6) {
            Vec3 cur = this.getDeltaMovement();
            double curSpeed = cur.length();
            double fallbackSpeed = Math.max(curSpeed, 1.8);
            if (cur.lengthSqr() < 1e-6) {
                Vec3 forward = this.getLookAngle().normalize().scale(fallbackSpeed);
                this.setDeltaMovement(forward);
            } else {
                this.setDeltaMovement(cur.normalize().scale(fallbackSpeed));
            }
            return;
        }

        Vec3 desiredDir = toTarget.normalize();

        desiredDir = desiredDir.add(0, 0.08, 0).normalize();

        Vec3 current = this.getDeltaMovement();
        double curSpeed = current.length();
        double minSpeed = 1.8;
        double maxSpeed = 7.0;
        double speed = Math.max(curSpeed, minSpeed);
        speed = Math.min(speed, maxSpeed);

        Vec3 desired = desiredDir.scale(speed);

        double blend = 0.25;
        Vec3 newMotion = current.lerp(desired, blend);

        double minY = -0.6;
        double maxY = 1.2;
        if (newMotion.y < minY) newMotion = new Vec3(newMotion.x, minY, newMotion.z);
        if (newMotion.y > maxY) newMotion = new Vec3(newMotion.x, maxY, newMotion.z);

        if (newMotion.length() < 0.5) {
            newMotion = newMotion.normalize().scale(minSpeed);
        }

        this.setDeltaMovement(newMotion);

        double horiz = Math.sqrt(newMotion.x * newMotion.x + newMotion.z * newMotion.z);
        float yRotDeg = (float) (Math.atan2(newMotion.x, newMotion.z) * (180D / Math.PI));
        float xRotDeg = (float) (Math.atan2(newMotion.y, horiz) * (180D / Math.PI));
        this.setYRot(yRotDeg);
        this.setXRot(xRotDeg);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        //super.onHitEntity(result);

        if (this.level().isClientSide) return;

        Entity hit = result.getEntity();
        if (hit == this.getOwner()) return;
        /*Entity shooter = this.getOwner();
        if (!(shooter instanceof Player player)) return;

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        boolean shooterHasEraser =
                main.getItem() instanceof com.test.eraser.Items.Eraser_Item
                        || off.getItem() instanceof com.test.eraser.Items.Eraser_Item;
        if (!shooterHasEraser) return;*/

        if (hit instanceof LivingEntity living) if (living instanceof ILivingEntity hit_) hit_.instantKill((Player)this.getOwner(), false);
        this.remove(RemovalReason.KILLED);
    }

}
