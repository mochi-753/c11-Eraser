package com.test.eraser.mixin.client;

import com.test.eraser.logic.ILivingEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.text2speech.Narrator.LOGGER;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void onAddEntity(int id, Entity entity, CallbackInfo ci) {
        if(entity instanceof ILivingEntity erase && erase.isErased()) {
            LOGGER.info("[Eraser Client] cancel addEntity id={} uuid={} class={} pos={}",
                    id, entity.getUUID(), entity.getClass().getSimpleName(), entity.blockPosition());
            ci.cancel();
        }
    }
}
