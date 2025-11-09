package com.test.eraser.network;

import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.network.packets.EraseEntityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {
    public static void handleEraseEntity(EraseEntityPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level != null) {
            Entity e = level.getEntity(msg.entityId);
            if (e instanceof ILivingEntity erased) {
                //erased.setErased(true);
                erased.eraseClientEntity((LivingEntity) e);
            }
        }
    }
}
