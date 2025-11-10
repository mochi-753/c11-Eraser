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
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class EraseEntityPacket {
    public final UUID entityUuid;

    public EraseEntityPacket(UUID entityUuid) {
        this.entityUuid = entityUuid;
    }

    public static void encode(EraseEntityPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityUuid);
    }

    public static EraseEntityPacket decode(FriendlyByteBuf buf) {
        return new EraseEntityPacket(buf.readUUID());
    }

    public static void handle(EraseEntityPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                System.out.println("Received EraseEntityPacket for UUID: " + msg.entityUuid);
                ClientPacketHandler.handleEraseEntity(msg);
            }
        });
        context.setPacketHandled(true);
    }
}
