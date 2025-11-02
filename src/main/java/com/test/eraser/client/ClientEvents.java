package com.test.eraser.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.test.eraser.additional.ModItems;
import com.test.eraser.additional.ModKeyBindings;
import com.test.eraser.client.renderer.ShieldEffectRenderer;
import com.test.eraser.network.PacketHandler;
import com.test.eraser.network.packets.EraserRangeAttackPacket;
import com.test.eraser.network.packets.RayCastPacket;
import com.test.eraser.network.packets.WorldDestroyerChangeModePacket;
import com.test.eraser.utils.DestroyMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.level == null) return;
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.getItem() == ModItems.ERASER_ITEM.get()) {
            if (mc.player.isShiftKeyDown()) {
                double radius = 10.0;
                AABB area = mc.player.getBoundingBox().inflate(radius);

                List<LivingEntity> targets = mc.level.getEntitiesOfClass(
                        LivingEntity.class,
                        area,
                        e -> e != mc.player
                );

                for (LivingEntity target : targets) {
                    target.setGlowingTag(true);
                }
            } else {
                for (Entity e : mc.level.entitiesForRendering()) {
                    e.setGlowingTag(false);
                }
            }
        }
        if (stack.getItem() == ModItems.WORLD_DESTROYER.get()) {
            if (ModKeyBindings.TOGGLE_RANGE.consumeClick()) {
                DestroyMode current = DestroyMode.getMode(mc.player.getMainHandItem());

                DestroyMode next = DestroyMode.values()[(current.ordinal() + 1) % DestroyMode.values().length];
                ItemStack held = mc.player.getMainHandItem();
                    /*mc.player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("Mode: " + next.name()),
                            true
                    );*/

                if (mc.player.isShiftKeyDown()) {
                    boolean nextSilk = !DestroyMode.isSilkTouchEnabled(held);

                    PacketHandler.CHANNEL.sendToServer(new WorldDestroyerChangeModePacket(current, nextSilk));

                } else {
                    boolean silk = DestroyMode.isSilkTouchEnabled(held);

                    PacketHandler.CHANNEL.sendToServer(new WorldDestroyerChangeModePacket(next, silk));
                }
            }
        }

    }

    @SubscribeEvent
    public static void onInput(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack stack = mc.player.getMainHandItem();
        if (event.getButton() == 1 && event.getAction() == 1 && mc.player.isShiftKeyDown() && stack.getItem() == ModItems.ERASER_ITEM.get()) {
            PacketHandler.CHANNEL.sendToServer(new EraserRangeAttackPacket());
        }
        if (event.getButton() == 0 && event.getAction() == 0 && stack.getItem() == ModItems.ERASER_ITEM.get()) {
            HitResult hit = mc.player.pick(64.0D, 0.0F, false);
            if (hit.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hit;
                int id = entityHit.getEntity().getId();

                PacketHandler.CHANNEL.sendToServer(new RayCastPacket(id));
            }
        }

    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

            ShieldEffectRenderer.render(poseStack, buffer, event.getPartialTick());

            buffer.endBatch();
        }
    }


}

