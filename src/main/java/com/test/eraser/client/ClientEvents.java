package com.test.eraser.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.test.eraser.additional.ModItems;
import com.test.eraser.additional.ModKeyBindings;
import com.test.eraser.client.renderer.ShieldEffectRenderer;
import com.test.eraser.logic.ILivingEntity;
import com.test.eraser.mixin.client.BossHelthOverlayAccessor;
import com.test.eraser.network.PacketHandler;
import com.test.eraser.network.packets.EraserRangeAttackPacket;
import com.test.eraser.network.packets.RayCastPacket;
import com.test.eraser.network.packets.WorldDestroyerChangeModePacket;
import com.test.eraser.utils.DestroyMode;
import com.test.eraser.utils.RenderUtils;
import com.test.eraser.utils.Res;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.function.Predicate;

import static com.test.eraser.logic.DestroyBlock.QueueRenderBreakBlock;
import static com.test.eraser.utils.RenderUtils.renderBlockList;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    public static final List<Entity> erasedEntities = new ArrayList<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc == null || mc.player == null || mc.level == null) return;
        HitResult hit = mc.hitResult;

        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            boolean same_id = DestroyMode.getMode(mc.player.getMainHandItem()) == DestroyMode.SAME_ID || DestroyMode.getMode(mc.player.getMainHandItem()) == DestroyMode.SAME_ID_ORE;
            Predicate<BlockState> accept = state -> !state.isAir();
            BlockState LookBlockState = mc.level.getBlockState(blockHit.getBlockPos());
            if(DestroyMode.getMode(mc.player.getMainHandItem()) == DestroyMode.SAME_ID_ORE) {
                TagKey<Block> FORGE_ORES = BlockTags.create(Res.getResource("forge", "ores"));
                accept = state -> state.is(FORGE_ORES) || state.is(BlockTags.LOGS);
            }
            else if(DestroyMode.getMode(mc.player.getMainHandItem()) == DestroyMode.SAME_ID){
                accept = state -> state.is(LookBlockState.getBlock());
            }
            if(DestroyMode.getMode(mc.player.getMainHandItem()) == DestroyMode.NORMAL || mc.player.getMainHandItem().getItem() != ModItems.WORLD_DESTROYER.get() ) {
                RenderQueue.clear();
            }
            else QueueRenderBreakBlock(mc.level, mc.player, blockHit.getBlockPos(),DestroyMode.getMode(mc.player.getMainHandItem()),same_id,32,accept);
        }else RenderQueue.clear();
        erase();
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
        if (event.getButton() == 0 && (stack.getItem() == ModItems.ERASER_ITEM.get() || stack.getItem() == ModItems.WORLD_DESTROYER.get())) {

            HitResult hit = mc.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHit = (EntityHitResult) hit;
                int id = entityHit.getEntity().getId();
                PacketHandler.CHANNEL.sendToServer(new RayCastPacket(id));
            }
        }
    }

    /*@SubscribeEvent //shitty shield effect rendering
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

            ShieldEffectRenderer.render(poseStack, buffer, event.getPartialTick());

            buffer.endBatch();
        }
    }*/

    private static final Map<UUID, Long> lastUpdate = new HashMap<>();

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Pre<LivingEntity, ?> event) {
        LivingEntity entity = event.getEntity();
        UUID uuid = entity.getUUID();

        if (entity instanceof ILivingEntity living && living.isErased()) {
            long now = System.currentTimeMillis();
            long last = lastUpdate.getOrDefault(uuid, 0L);

            if (now - last >= 50 && !entity.isDeadOrDying()) {//1tick
                entity.deathTime++;
                entity.setPose(Pose.DYING);
                lastUpdate.put(uuid, now);
            }
            if (entity.deathTime > 20) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ILivingEntity living) {
            if (living.isErased()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc == null || mc.player == null || mc.level == null) return;

                //System.out.println(Component.literal("[Eraser] Prevented joining erased entity to level: " + event.getEntity().toString()));
                event.setCanceled(true);
            }
        }
    }

    public static boolean erase() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        int[] ids = erasedEntities.stream()
                .mapToInt(Entity::getId)
                .toArray();

        for (int id : ids) {
            Entity e = level.getEntity(id);
            if (e != null) {
                ClientPacketListener connection = mc.getConnection();

                ClientboundRemoveEntitiesPacket packet =
                        new ClientboundRemoveEntitiesPacket(e.getId());

                packet.handle(connection);
                return true;
            }

        }
        return false;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft mc = Minecraft.getInstance();
            PoseStack poseStack = event.getPoseStack();
            Vec3 camera = event.getCamera().getPosition();

            /*for (RenderQueue.RenderEntry entry : RenderQueue.getEntries()) {
                if(entry == null) break;
                RenderUtils.renderBlockBox(poseStack, camera, entry.pos, entry.color);
            }*/
            renderBlockList(event.getPoseStack(), event.getCamera().getPosition(), RenderQueue.getPositions(), 0xFFFFFFFF);

            //RenderQueue.clear();
        }
    }

}

