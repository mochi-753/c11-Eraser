package com.test.eraser.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class TintingVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float r, g, b, a;

    public TintingVertexConsumer(VertexConsumer delegate, float r, float g, float b, float a) {
        this.delegate = delegate;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return delegate.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        // 元の色に乗算
        int nr = Math.min(255, (int) (red * r));
        int ng = Math.min(255, (int) (green * g));
        int nb = Math.min(255, (int) (blue * b));
        int na = Math.min(255, (int) (alpha * a));
        return delegate.color(nr, ng, nb, na);
    }

    // 他のメソッドはそのまま委譲
    @Override
    public VertexConsumer uv(float u, float v) {
        return delegate.uv(u, v);
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return delegate.overlayCoords(u, v);
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return delegate.uv2(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return delegate.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        delegate.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}
