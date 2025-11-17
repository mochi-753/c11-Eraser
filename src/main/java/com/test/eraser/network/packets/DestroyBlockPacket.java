package com.test.eraser.network.packets;

import com.test.eraser.items.Eraser_Item;
import com.test.eraser.logic.DestroyBlock;
import com.test.eraser.utils.DestroyMode;
import com.test.eraser.utils.WorldDestroyerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class  DestroyBlockPacket {
    private final BlockPos pos;
    private final DestroyMode mode;

    public DestroyBlockPacket(BlockPos pos, DestroyMode mode) {
        this.pos = pos;
        this.mode = mode;
    }

    public static void encode(DestroyBlockPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeEnum(msg.mode);
    }

    public static DestroyBlockPacket decode(FriendlyByteBuf buf) {
        return new DestroyBlockPacket(buf.readBlockPos(), buf.readEnum(DestroyMode.class));
    }


    public static void handle(DestroyBlockPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ItemStack held = player.getMainHandItem();

            WorldDestroyerUtils.destroyblock(held, player);
        });
        ctx.get().setPacketHandled(true);
    }
}