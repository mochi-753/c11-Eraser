package com.test.eraser.network.packets;

import com.test.eraser.additional.ModItems;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.mixin.client.BossHelthOverlayAccessor;
import com.test.eraser.utils.Eraser_Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
            Item held = sender.getMainHandItem().getItem();
            if(held != ModItems.ERASER_ITEM.get() && held != ModItems.WORLD_DESTROYER.get()) {
                return;//ah
            }
            if (sender != null) {
                Entity target = sender.level().getEntity(msg.entityId);
                if(sender.level().isClientSide()) return;
                if (target != null && sender.getPosition(0).distanceTo(target.getPosition(0)) <= 4) {
                    Minecraft mc = Minecraft.getInstance();
                    ((BossHelthOverlayAccessor)mc.gui.getBossOverlay()).getEvents().remove(target.getUUID());
                    Eraser_Utils.killIfParentFound(target, (Player)sender, 32);

                    //System.out.println("RayCastPacket: processed entity ID " + msg.entityId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
