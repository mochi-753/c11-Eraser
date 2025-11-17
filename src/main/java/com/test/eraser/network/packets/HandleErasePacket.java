package com.test.eraser.network.packets;

import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.network.ClientPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class HandleErasePacket {

    public HandleErasePacket() {}

    public static void encode(HandleErasePacket msg, FriendlyByteBuf buf) {}

    public static HandleErasePacket decode(FriendlyByteBuf buf) {return  new HandleErasePacket();}

    public static void handle(HandleErasePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (player instanceof ILivingEntity player_){
                    player_.unmarkErased(player.getUUID());
                    player_.setErased(false);
                }

            }
        });
        ctx.get().setPacketHandled(true);
    }
}
