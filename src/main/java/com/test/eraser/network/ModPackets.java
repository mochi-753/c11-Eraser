package com.test.eraser.network;

import com.test.eraser.network.packets.EraserRangeAttackPacket;
import com.test.eraser.network.packets.RayCastPacket;
import com.test.eraser.network.packets.ShieldEffectPacket;
import com.test.eraser.network.packets.WorldDestroyerChangeModePacket;
import net.minecraftforge.network.NetworkDirection;

import java.util.Optional;

public class ModPackets {
    private static int id = 0;

    public static void register() {
        PacketHandler.CHANNEL.registerMessage(
                id++, EraserRangeAttackPacket.class,
                EraserRangeAttackPacket::encode,
                EraserRangeAttackPacket::decode,
                EraserRangeAttackPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        PacketHandler.CHANNEL.registerMessage(
                id++, WorldDestroyerChangeModePacket.class,
                WorldDestroyerChangeModePacket::encode,
                WorldDestroyerChangeModePacket::decode,
                WorldDestroyerChangeModePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        PacketHandler.CHANNEL.registerMessage(
                id++, ShieldEffectPacket.class,
                ShieldEffectPacket::encode,
                ShieldEffectPacket::decode,
                ShieldEffectPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        PacketHandler.CHANNEL.registerMessage(
                id++, RayCastPacket.class,
                RayCastPacket::encode,
                RayCastPacket::decode,
                RayCastPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

    }
}