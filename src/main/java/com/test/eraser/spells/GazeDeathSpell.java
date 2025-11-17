package com.test.eraser.spells;

import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.utils.Res;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class GazeDeathSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(3)
            .setCooldownSeconds(0)
            .build();

    public GazeDeathSpell() {
        super();
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
        this.baseManaCost = 10;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {

        LivingEntity target = getLookTarget(caster, 32.0);
        if (target == null) {
            return;
        }

        switch (spellLevel) {
            case 1 -> {
                float hp = target.getHealth();
                target.hurt(level.damageSources().magic(), target.getHealth());
            }
            case 2 -> {
                target.setHealth(0.0f);
                target.die(level.damageSources().magic());
            }
            case 3 -> {
                if (target instanceof ILivingEntity erased) {
                    if (caster instanceof Player p) {
                        erased.instantKill(p);
                    } else {
                        erased.instantKill();
                    }
                }
            }
            default -> {
                float hp = target.getHealth();
                target.hurt(level.damageSources().magic(), Math.max(hp, 1.0f));
            }
        }

    }

    private LivingEntity getLookTarget(LivingEntity caster, double range) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        Vec3 end = eye.add(look.scale(range));

        ClipContext ctx = new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster);
        BlockHitResult blockHit = caster.level().clip(ctx);
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB sweep = caster.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        List<LivingEntity> entities = caster.level().getEntitiesOfClass(LivingEntity.class, sweep,
                e -> e.isAlive() && e != caster);

        LivingEntity best = null;
        double bestDist = range * range;
        for (LivingEntity e : entities) {
            AABB aabb = e.getBoundingBox().inflate(0.3);
            Optional<Vec3> hit = aabb.clip(eye, end);
            if (hit.isPresent()) {
                double dist = eye.distanceToSqr(hit.get());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = e;
                }
            }
        }
        return best;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return  Res.getResource("eraser", "gaze_death");
    }
}