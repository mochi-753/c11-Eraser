package com.test.eraser.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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

        // 必要なら透過・太さ設定
        // RenderSystem.disableDepthTest();
        // RenderSystem.lineWidth(2.0F);

        VertexConsumer builder = getBuffer(RenderType.lines());

    /*
     EDGES: 各行は {x1,y1,z1, x2,y2,z2, nx,ny,nz}
     頂点は常に「小さい座標 -> 大きい座標」の向きで統一（視認性のため）
    */
        final float[][] EDGES = new float[][]{
                // bottom y=0 (四辺) - 頂点は (min -> max) の順
                {0,0,0, 1,0,0, 0,-1,0}, // edge 0: (0,0,0)-(1,0,0)
                {1,0,0, 1,0,1, 0,-1,0}, // edge 1: (1,0,0)-(1,0,1)
                {0,0,1, 1,0,1, 0,-1,0}, // edge 2: (0,0,1)-(1,0,1)
                {0,0,0, 0,0,1, 0,-1,0}, // edge 3: (0,0,0)-(0,0,1)

                // top y=1 (四辺)
                {0,1,0, 1,1,0, 0,1,0},  // edge 4: (0,1,0)-(1,1,0)
                {1,1,0, 1,1,1, 0,1,0},  // edge 5: (1,1,0)-(1,1,1)
                {0,1,1, 1,1,1, 0,1,0},  // edge 6: (0,1,1)-(1,1,1)
                {0,1,0, 0,1,1, 0,1,0},  // edge 7: (0,1,0)-(0,1,1)

                // west x=0 vertical edges (Z 0 and 1)
                {0,0,0, 0,1,0, -1,0,0}, // edge 8:  (0,0,0)-(0,1,0)
                {0,0,1, 0,1,1, -1,0,0}, // edge 9:  (0,0,1)-(0,1,1)

                // east x=1 vertical edges (Z 0 and 1)
                {1,0,0, 1,1,0, 1,0,0},  // edge10:  (1,0,0)-(1,1,0)
                {1,0,1, 1,1,1, 1,0,0},  // edge11:  (1,0,1)-(1,1,1)
        };

    /*
     NEIGHBORS: 各辺が共有される2つの隣接ブロックを明示
     例: bottom front edge (0,0,0)-(1,0,0) は下(0,-1,0) と north(0,0,-1) を共有
     各エントリは {{dx1,dy1,dz1},{dx2,dy2,dz2}}
    */
        final int[][][] NEIGHBORS = new int[][][]{
                // bottom edges
                {{0,-1,0}, {0,0,-1}}, // edge0: (0,0,0)-(1,0,0)  下 / 北
                {{0,-1,0}, {1,0,0}},  // edge1: (1,0,0)-(1,0,1)  下 / 東
                {{0,-1,0}, {0,0,1}},  // edge2: (0,0,1)-(1,0,1)  下 / 南
                {{0,-1,0}, {-1,0,0}}, // edge3: (0,0,0)-(0,0,1)  下 / 西

                // top edges
                {{0,1,0},  {0,0,-1}}, // edge4: (0,1,0)-(1,1,0)  上 / 北
                {{0,1,0},  {1,0,0}},  // edge5: (1,1,0)-(1,1,1)  上 / 東
                {{0,1,0},  {0,0,1}},  // edge6: (0,1,1)-(1,1,1)  上 / 南
                {{0,1,0},  {-1,0,0}}, // edge7: (0,1,0)-(0,1,1)  上 / 西

                // west vertical (x=0)
                {{-1,0,0}, {0,0,-1}}, // edge8: (0,0,0)-(0,1,0)  西 / 北
                {{-1,0,0}, {0,0,1}},  // edge9: (0,0,1)-(0,1,1)  西 / 南

                // east vertical (x=1)
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

                // 2つの隣接どちらかが planned に含まれていればその辺をスキップ
                if (planned.contains(n1) || planned.contains(n2)) continue;
                boolean n1p = planned.contains(n1);
                boolean n2p = planned.contains(n2);

                System.out.printf("DRAW edge %d at %s; n1=%s (%b), n2=%s (%b)%n",
                        i, pos, n1, n1p, n2, n2p);

                // 描画（頂点は常に同じ向き: 小さい座標 -> 大きい座標）
                drawLine(builder, matrix, normalMatrix,
                        e[0], e[1], e[2],
                        e[3], e[4], e[5],
                        r, g, b, a,
                        e[6], e[7], e[8]);
            }

            poseStack.popPose();
        }

        endBatch(RenderType.lines());

        // 元に戻す場合は解除
        // RenderSystem.enableDepthTest();
        // RenderSystem.lineWidth(1.0F);
    }

    // ヘルパー（POSITION_COLOR_NORMAL を満たす）
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
}
