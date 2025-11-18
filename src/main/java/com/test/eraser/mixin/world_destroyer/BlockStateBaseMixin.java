package com.test.eraser.mixin.world_destroyer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {

    @Inject(
            method = {"getDestroyProgress"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void ongetDestroyProgress(Player player, BlockGetter Level, BlockPos Pos, CallbackInfoReturnable<Float> cir) {
        if (Level instanceof Level level && player.isShiftKeyDown()) {
            if(!level.getBlockState(Pos).isAir()) cir.setReturnValue(0.0f);
        }

    }
}
