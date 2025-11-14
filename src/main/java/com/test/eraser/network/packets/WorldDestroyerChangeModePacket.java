package com.test.eraser.network.packets;

import com.test.eraser.Config;
import com.test.eraser.utils.DestroyMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;


public class WorldDestroyerChangeModePacket {
    public final boolean silkEnabled;
    private final DestroyMode mode;

    public WorldDestroyerChangeModePacket(DestroyMode mode, boolean silkEnabled) {
        this.mode = mode;
        this.silkEnabled = silkEnabled;
    }

    public static void encode(WorldDestroyerChangeModePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.mode.ordinal());
        buf.writeBoolean(msg.silkEnabled);
    }

    public static WorldDestroyerChangeModePacket decode(FriendlyByteBuf buf) {
        DestroyMode mode = DestroyMode.values()[buf.readInt()];
        boolean silk = buf.readBoolean();
        return new WorldDestroyerChangeModePacket(mode, silk);
    }

    public static void handle(WorldDestroyerChangeModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack held = player.getMainHandItem();

                DestroyMode.setMode(held, msg.mode);
                DestroyMode.setSilkTouch(held, msg.silkEnabled);

                if(Config.CHENGEMODE_MESSAGE.get())
                    player.displayClientMessage(
                        Component.literal(
                                "Mode: " + msg.mode.name() +
                                        (msg.silkEnabled ? " [Silk ON]" : " [Silk OFF]")
                        ),
                        true
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
