package com.test.eraser.network.packets;

import com.test.eraser.logic.ILivingEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RayCastPacket {
    private final int entityId;

    public RayCastPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(RayCastPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
    }

    public static RayCastPacket decode(FriendlyByteBuf buf) {
        return new RayCastPacket(buf.readInt());
    }

    public static void handle(RayCastPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                Entity target = sender.level().getEntity(msg.entityId);
                if (target != null) {
                    if (target instanceof ILivingEntity target_) target_.instantKill();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
