package com.test.eraser.network;

import com.test.eraser.Eraser;
import com.test.eraser.utils.Res;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Res.getResource(Eraser.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        ModPackets.register();
    }

    public static void sendToPlayer(Object msg, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }

}
