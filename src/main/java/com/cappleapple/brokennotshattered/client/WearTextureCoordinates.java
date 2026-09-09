package com.cappleapple.brokennotshattered.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Remaps only the base texture buffer. Glint continues to receive the original atlas coordinates. */
public final class WearTextureCoordinates implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float u0, v0, width, height;

    public WearTextureCoordinates(VertexConsumer delegate, TextureAtlasSprite sprite) {
        this(delegate, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    WearTextureCoordinates(VertexConsumer delegate, float u0, float v0, float u1, float v1) {
        this.delegate = delegate;
        this.u0 = u0;
        this.v0 = v0;
        this.width = u1 - u0;
        this.height = v1 - v0;
    }

    @Override public VertexConsumer addVertex(float x, float y, float z) { delegate.addVertex(x, y, z); return this; }
    @Override public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r, g, b, a); return this; }
    @Override public VertexConsumer setUv(float u, float v) { delegate.setUv((u - u0) / width, (v - v0) / height); return this; }
    @Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
    @Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }
    @Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
}
