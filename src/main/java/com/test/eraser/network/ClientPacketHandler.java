package com.test.eraser.network;

import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.network.packets.EraseEntityPacket;
import com.test.eraser.utils.TaskScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static com.mojang.text2speech.Narrator.LOGGER;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {
    public static void handleEraseEntity(EraseEntityPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level != null) {
            Entity e = null;
            for (Entity ent : mc.level.entitiesForRendering()) {
                if (ent.getUUID().equals(msg.entityUuid)) {
                    e = ent;
                    break;
                }
            }
            if (e == null) {
                return;
            }

            if (((LivingEntity)e) instanceof ILivingEntity erased) {
                //LOGGER.info("[Eraser] Received EraseEntityPacket for entity UUID: " + e.getUUID());
                //erased.setErased(true);
                erased.markErased(e.getUUID());

                if(!(e instanceof Player)) {
                    if(msg.skipAnimation) erased.eraseClientEntity();
                    else TaskScheduler.schedule(erased::eraseClientEntity, 22);
                }

            }
        }
    }

}
