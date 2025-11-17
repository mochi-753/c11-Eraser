package com.test.eraser.mixin.world_destroyer;

import com.test.eraser.logic.ILevelChunk;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.logic.IServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements IServerLevel {

    @Override
    public boolean forceSetBlock(BlockPos pos, BlockState newState, int flags, boolean isMoving) {
        ServerLevel self = (ServerLevel)(Object)this;

        if (self.isOutsideBuildHeight(pos)) {
            return false;
        }
        if (!self.isClientSide && self.isDebug()) {
            return false;
        }

        LevelChunk chunk = self.getChunkAt(pos);
        pos = pos.immutable();

        BlockState oldState = ((ILevelChunk)chunk)
                .forceSetBlockState(self, pos, newState, isMoving);

        if (oldState == null) {
            return false;
        }
        self.levelEvent(null, 2001, pos, Block.getId(oldState));
        self.sendBlockUpdated(pos, oldState, newState, flags);

        for (Direction direction : Direction.values()) {//block update
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = self.getBlockState(neighborPos);

            BlockState updatedState = neighborState.updateShape(direction.getOpposite(), self.getBlockState(neighborPos.relative(direction)), self, neighborPos, neighborPos.relative(direction));

            if (updatedState != neighborState) {
                ((IServerLevel)self).forceSetBlock(neighborPos, updatedState, flags, isMoving);
            }
        }
        return true;
    }
}