package com.cappleapple.brokennotshattered.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.util.Optional;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Retains the owning model's shader, blend, lightmap, output target and vertex format. */
public final class WearRenderType extends RenderType {
    private static final Cache<Key, RenderType> TYPES = CacheBuilder.newBuilder().maximumSize(1024).build();
    private final RenderType delegate;
    private final ResourceLocation texture;
    private final TextureAtlasSprite sprite;

    private WearRenderType(RenderType delegate, ResourceLocation texture, TextureAtlasSprite sprite, TextureState state) {
        super("broken_not_shattered_wear", delegate.format(), delegate.mode(), delegate.bufferSize(),
            delegate.affectsCrumbling(), delegate.sortOnUpload(), state::setup, state::clear);
        this.delegate = delegate;
        this.texture = texture;
        this.sprite = sprite;
    }

    public static RenderType wrap(RenderType delegate, ResourceLocation texture) {
        return wrap(delegate, texture, null);
    }

    public static RenderType wrap(RenderType delegate, ResourceLocation texture, TextureAtlasSprite sprite) {
        Key key = new Key(delegate, texture, sprite);
        RenderType existing = TYPES.getIfPresent(key);
        if (existing != null) return existing;
        RenderType result = new WearRenderType(delegate, texture, sprite, new TextureState(delegate, texture));
        TYPES.put(key, result);
        return result;
    }

    public static MultiBufferSource textureBuffers(MultiBufferSource original) {
        return original instanceof TextureBuffers ? original : new TextureBuffers(original);
    }

    private record TextureBuffers(MultiBufferSource original) implements MultiBufferSource {
        @Override
        public VertexConsumer getBuffer(RenderType type) {
            VertexConsumer buffer = original.getBuffer(type);
            if (buffer instanceof WearTextureCoordinates) return buffer;
            return type instanceof WearRenderType wear && wear.sprite != null
                ? new WearTextureCoordinates(buffer, wear.sprite) : buffer;
        }
    }

    @Override
    public Optional<RenderType> outline() {
        return delegate.outline().map(ignored -> RenderType.outline(texture));
    }

    @Override
    public boolean isOutline() {
        return delegate.isOutline();
    }

    private record Key(RenderType delegate, ResourceLocation texture, TextureAtlasSprite sprite) {}

    private static final class TextureState {
        private final RenderType delegate;
        private final ResourceLocation texture;
        private int previous;

        TextureState(RenderType delegate, ResourceLocation texture) {
            this.delegate = delegate;
            this.texture = texture;
        }

        void setup() {
            delegate.setupRenderState();
            previous = RenderSystem.getShaderTexture(0);
            RenderSystem.setShaderTexture(0, texture);
        }

        void clear() {
            RenderSystem.setShaderTexture(0, previous);
            delegate.clearRenderState();
        }
    }
}
