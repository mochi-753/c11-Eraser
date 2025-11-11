package com.test.eraser.mixin.eraser;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.utils.EraseEntityLookupBridge;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(value = EntityLookup.class, priority = 114514)
public abstract class EntityLookupMixin<T extends EntityAccess>
        implements EraseEntityLookupBridge<T>, EntityLookupAccessor<T> {

    @Unique
    private Int2ObjectMap<T> byId2 = new Int2ObjectLinkedOpenHashMap<>();
    @Unique
    private Map<UUID, T> byUuid2 = Maps.newHashMap();

    @Unique
    @Override
    public boolean eraseEntity(T entity) {
        if (entity == null) return false;
        UUID uuid = entity.getUUID();
        int id = entity.getId();

        boolean removed = false;
        if (byId2 == null || byUuid2 == null) return false;
        this.byUuid2.put(uuid, entity);
        this.byId2.put(entity.getId(), entity);

        Int2ObjectMap<T> vanillaById = this.getById();
        Map<UUID, T> vanillaByUuid = this.getByUuid();
        if (vanillaByUuid != null && vanillaByUuid.remove(uuid) != null) {
            this.setByUuid(vanillaByUuid);
            removed = true;
        }
        if (vanillaById != null && vanillaById.remove(id) != null) {
            this.setById(vanillaById);
            removed = true;
        }
        return removed;
    }

    /*@Inject(method = "add", at = @At("HEAD"), cancellable = true)
    public void onadd(T entity, CallbackInfo ci) {
        if (entity == null || byId2 == null || byUuid2 == null) return;
        UUID uuid = entity.getUUID();

        if (entity instanceof ILivingEntity) {
            if(((ILivingEntity) entity).isErased(entity.getUUID())){
                ci.cancel();
                return;
            }
        }
        this.byUuid2.put(uuid, entity);
        this.byId2.put(entity.getId(), entity);

    }*/

    @Inject(method = "remove", at = @At("RETURN"))
    public void onremove(T entity, CallbackInfo ci) {
        if (entity == null || byId2 == null || byUuid2 == null) return;

        /*this.byUuid2.remove(entity.getUUID());
        this.byId2.remove(entity.getId());*/
    }

    /*@Inject(method = "getEntity(I)Lnet/minecraft/world/level/entity/EntityAccess;", at = @At("RETURN"), cancellable = true)
    public void getEntityById(int Id, CallbackInfoReturnable<T> cir) {
        if(byId2 == null || byUuid2 == null) return;
        T e = this.byId2.get(Id);
        if (e != null) {
            if (e instanceof ILivingEntity living) {
                if (living.isErased()) {
                    System.out.println("wtf its still here? id: " + Id);
                    cir.setReturnValue(null);
                    cir.cancel();
                }
            }
        }
    }

    @Inject(method = "getEntity(Ljava/util/UUID;)Lnet/minecraft/world/level/entity/EntityAccess;", at = @At("RETURN"), cancellable = true)
    public void getEntityByUuid(UUID Uuid, CallbackInfoReturnable<T> cir) {
        if(byId2 == null || byUuid2 == null) return;
        T e = this.byUuid2.get(Uuid);
        if (e != null) {
            if (e instanceof ILivingEntity living) {
                if (living.isErased()) {
                    System.out.println("wtf its still here? uuid: " + Uuid);
                    cir.setReturnValue(null);
                    cir.cancel();
                }
            }
        }
    }*/

    /*@Inject(method = "getAllEntities", at = @At("RETURN"), cancellable = true)
    public void getAllEntitiesReturn(CallbackInfoReturnable<Iterable<T>> cir) {
        if (byId2 == null || byUuid2 == null) return;

        Iterable<T> orig = cir.getReturnValue();
        List<T> filtered = new ArrayList<>();

        for (T t : orig) {
            if (!byUuid2.containsKey(t.getUUID())) {
                filtered.add(t);
            }
        }

        cir.setReturnValue(filtered);
        cir.cancel();
    }*/

    @Inject(method = "count", at = @At("RETURN"), cancellable = true)
    public void Count(CallbackInfoReturnable<Integer> cir) {
        if(byId2 == null || byUuid2 == null) return;
        int extra = 0;
        for (T t : this.byUuid2.values()) {
            if (t instanceof ILivingEntity) {
                if (((ILivingEntity) t).isErased()) extra++;
            }
        }
        if (extra > 0) {
            cir.setReturnValue(!this.byUuid2.isEmpty() ? (extra + cir.getReturnValueI()) : cir.getReturnValueI());
        }
    }
    @Inject(
            method = {"<init>"},
            at = {@At("RETURN")}
    )
    private void oninit(CallbackInfo ci) {
        this.byId2 = new Int2ObjectLinkedOpenHashMap();
    }
}
