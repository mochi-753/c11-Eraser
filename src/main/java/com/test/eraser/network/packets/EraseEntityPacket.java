package com.test.eraser.network.packets;

import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.network.ClientPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EraseEntityPacket {
    public final int entityId;

    public EraseEntityPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(EraseEntityPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
    }

    public static EraseEntityPacket decode(FriendlyByteBuf buf) {
        return new EraseEntityPacket(buf.readVarInt());
    }

    public static void handle(EraseEntityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientPacketHandler.handleEraseEntity(msg);
            });
        });
        ctx.get().setPacketHandled(true);
    }

}