package com.test.eraser.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.*;
import java.util.function.Predicate;

public class RenderUtils {

    private RenderUtils() {}

    public static VertexConsumer getBuffer(RenderType type) {
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        return bufferSource.getBuffer(type);
    }

    public static void endBatch(RenderType type) {
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(type);
    }

    public static void renderBlockBox(PoseStack poseStack, Vec3 camera, BlockPos pos, int color) {
        AABB aabb = new AABB(pos);
        AABB identity = new AABB(0, 0, 0, 1, 1, 1);

        poseStack.pushPose();
        poseStack.translate(
                aabb.minX - camera.x,
                aabb.minY - camera.y,
                aabb.minZ - camera.z
        );

        LevelRenderer.renderLineBox(
                poseStack,
                getBuffer(RenderType.lines()),
                identity,
                (float)(color >> 16 & 255) / 255.0F, // R
                (float)(color >> 8 & 255) / 255.0F,  // G
                (float)(color & 255) / 255.0F,       // B
                (float)(color >> 24 & 255) / 255.0F  // A
        );

        endBatch(RenderType.lines());
        poseStack.popPose();
    }

    public static void renderBlockList(PoseStack poseStack, Vec3 camera,
                                       List<BlockPos> positions, int color) {
        Set<BlockPos> planned = new HashSet<>(positions);

        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        float a = (float)(color >> 24 & 255) / 255.0F;

        VertexConsumer builder = getBuffer(RenderType.lines());

        final float[][] EDGES = new float[][]{
                {0,0,0, 1,0,0, 0,-1,0}, // edge 0: (0,0,0)-(1,0,0)
                {1,0,0, 1,0,1, 0,-1,0}, // edge 1: (1,0,0)-(1,0,1)
                {0,0,1, 1,0,1, 0,-1,0}, // edge 2: (0,0,1)-(1,0,1)
                {0,0,0, 0,0,1, 0,-1,0}, // edge 3: (0,0,0)-(0,0,1)

                {0,1,0, 1,1,0, 0,1,0},  // edge 4: (0,1,0)-(1,1,0)
                {1,1,0, 1,1,1, 0,1,0},  // edge 5: (1,1,0)-(1,1,1)
                {0,1,1, 1,1,1, 0,1,0},  // edge 6: (0,1,1)-(1,1,1)
                {0,1,0, 0,1,1, 0,1,0},  // edge 7: (0,1,0)-(0,1,1)

                {0,0,0, 0,1,0, -1,0,0}, // edge 8:  (0,0,0)-(0,1,0)
                {0,0,1, 0,1,1, -1,0,0}, // edge 9:  (0,0,1)-(0,1,1)

                {1,0,0, 1,1,0, 1,0,0},  // edge10:  (1,0,0)-(1,1,0)
                {1,0,1, 1,1,1, 1,0,0},  // edge11:  (1,0,1)-(1,1,1)
        };

        final int[][][] NEIGHBORS = new int[][][]{
                {{0,-1,0}, {0,0,-1}}, // edge0: (0,0,0)-(1,0,0)  下 / 北
                {{0,-1,0}, {1,0,0}},  // edge1: (1,0,0)-(1,0,1)  下 / 東
                {{0,-1,0}, {0,0,1}},  // edge2: (0,0,1)-(1,0,1)  下 / 南
                {{0,-1,0}, {-1,0,0}}, // edge3: (0,0,0)-(0,0,1)  下 / 西

                {{0,1,0},  {0,0,-1}}, // edge4: (0,1,0)-(1,1,0)  上 / 北
                {{0,1,0},  {1,0,0}},  // edge5: (1,1,0)-(1,1,1)  上 / 東
                {{0,1,0},  {0,0,1}},  // edge6: (0,1,1)-(1,1,1)  上 / 南
                {{0,1,0},  {-1,0,0}}, // edge7: (0,1,0)-(0,1,1)  上 / 西

                {{-1,0,0}, {0,0,-1}}, // edge8: (0,0,0)-(0,1,0)  西 / 北
                {{-1,0,0}, {0,0,1}},  // edge9: (0,0,1)-(0,1,1)  西 / 南

                {{1,0,0},  {0,0,-1}}, // edge10: (1,0,0)-(1,1,0) 東 / 北
                {{1,0,0},  {0,0,1}},  // edge11: (1,0,1)-(1,1,1) 東 / 南
        };

        for (BlockPos pos : positions) {
            poseStack.pushPose();
            poseStack.translate(pos.getX() - camera.x,
                    pos.getY() - camera.y,
                    pos.getZ() - camera.z);

            Matrix4f matrix = poseStack.last().pose();
            Matrix3f normalMatrix = poseStack.last().normal();

            for (int i = 0; i < EDGES.length; i++) {
                float[] e = EDGES[i];
                int[][] ns = NEIGHBORS[i];

                BlockPos n1 = pos.offset(ns[0][0], ns[0][1], ns[0][2]);
                BlockPos n2 = pos.offset(ns[1][0], ns[1][1], ns[1][2]);

                if (planned.contains(n1) || planned.contains(n2)) continue;
                boolean n1p = planned.contains(n1);
                boolean n2p = planned.contains(n2);

                System.out.printf("DRAW edge %d at %s; n1=%s (%b), n2=%s (%b)%n",
                        i, pos, n1, n1p, n2, n2p);

                drawLine(builder, matrix, normalMatrix,
                        e[0], e[1], e[2],
                        e[3], e[4], e[5],
                        r, g, b, a,
                        e[6], e[7], e[8]);
            }

            poseStack.popPose();
        }

        endBatch(RenderType.lines());

    }

    private static void drawLine(VertexConsumer builder, Matrix4f matrix, Matrix3f normalMatrix,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float r, float g, float b, float a,
                                 float nx, float ny, float nz) {
        builder.vertex(matrix, x1, y1, z1)
                .color(r, g, b, a)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
        builder.vertex(matrix, x2, y2, z2)
                .color(r, g, b, a)
                .normal(normalMatrix, nx, ny, nz)
                .endVertex();
    }

    public static void drawBillboardQuad(PoseStack poseStack, VertexConsumer consumer,
                                         float r, float g, float b, float a) {
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        consumer.vertex(matrix, -0.5f, -0.5f, 0).color(r, g, b, a).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(matrix, -0.5f, 0.5f, 0).color(r, g, b, a).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(matrix, 0.5f, 0.5f, 0).color(r, g, b, a).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
        consumer.vertex(matrix, 0.5f, -0.5f, 0).color(r, g, b, a).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normal, 0, 0, -1).endVertex();
    }
}
