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

    @Override public VertexConsumer vertex(double x, double y, double z) { delegate.vertex(x, y, z); return this; }
    @Override public VertexConsumer color(int r, int g, int b, int a) { delegate.color(r, g, b, a); return this; }
    @Override public VertexConsumer uv(float u, float v) { delegate.uv((u - u0) / width, (v - v0) / height); return this; }
    @Override public VertexConsumer overlayCoords(int u, int v) { delegate.overlayCoords(u, v); return this; }
    @Override public VertexConsumer uv2(int u, int v) { delegate.uv2(u, v); return this; }
    @Override public VertexConsumer normal(float x, float y, float z) { delegate.normal(x, y, z); return this; }
    @Override public void endVertex(){delegate.endVertex();}
    @Override public void defaultColor(int r,int g,int b,int a){delegate.defaultColor(r,g,b,a);}
    @Override public void unsetDefaultColor(){delegate.unsetDefaultColor();}
}
