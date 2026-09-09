package com.cappleapple.brokennotshattered.client;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** A resolved model with per-sprite worn textures and optional separated geometry. */
@SuppressWarnings("deprecation")
public final class BrokenIconModel implements BakedModel {
    private static final Map<ItemStack, Map<CacheKey, Geometry>> CACHE = new MapMaker().weakKeys().makeMap();
    private final BakedModel delegate;
    private final ItemStack owner;
    private final WearPattern pattern;
    private final boolean split;
    private final boolean caps;
    private final Group group;
    private final ResourceLocation texture;

    private BrokenIconModel(BakedModel delegate, ItemStack owner, WearPattern pattern, boolean split, boolean caps,
                            Group group, ResourceLocation texture) {
        this.delegate = delegate;
        this.owner = owner;
        this.pattern = pattern;
        this.split = split;
        this.caps = caps;
        this.group = group;
        this.texture = texture;
    }

    public static BakedModel wrap(BakedModel model, ItemStack owner, WearPattern pattern, boolean split, boolean caps) {
        return new BrokenIconModel(model, owner, pattern, split, caps, null, null);
    }

    static void clearCache() {
        CACHE.clear();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        if (group == null) return delegate.getQuads(state, side, random);
        return group.original.getOrDefault(side, List.of());
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        Map<CacheKey, Geometry> cache = CACHE.computeIfAbsent(owner, ignored -> new LinkedHashMap<>());
        List<BakedModel> passes = new ArrayList<>();
        for (BakedModel pass : delegate.getRenderPasses(stack, fabulous)) {
            List<SourceQuad> sources = collect(pass);
            CacheKey key = new CacheKey(pass, pattern, split, caps);
            Geometry geometry = cache.get(key);
            if (geometry == null || !geometry.sources.equals(sources)) {
                geometry = build(sources);
                if (cache.size() >= 8) cache.clear();
                cache.put(key, geometry);
            }
            for (Group part : geometry.groups) {
                passes.add(new BrokenIconModel(pass, owner, pattern, split, caps, part, WornTextures.sprite(part.sprite, pattern)));
            }
        }
        return passes;
    }

    private static List<SourceQuad> collect(BakedModel model) {
        List<SourceQuad> sources = new ArrayList<>();
        RandomSource random = RandomSource.create(42);
        for (int face = 0; face <= Direction.values().length; face++) {
            Direction side = face == Direction.values().length ? null : Direction.values()[face];
            random.setSeed(42);
            for (BakedQuad quad : model.getQuads(null, side, random)) {
                sources.add(new SourceQuad(side, quad, Arrays.hashCode(quad.getVertices())));
            }
        }
        return sources;
    }

    private Geometry build(List<SourceQuad> sources) {
        Map<TextureAtlasSprite, Group> groups = new LinkedHashMap<>();
        // Caps need both front/back depth information, including direction-specific quads.
        Map<TextureAtlasSprite, List<BakedQuad>> bySprite = new LinkedHashMap<>();
        for (SourceQuad source : sources) {
            bySprite.computeIfAbsent(source.quad.getSprite(), ignored -> new ArrayList<>()).add(source.quad);
        }
        for (var entry : bySprite.entrySet()) {
            Group part = new Group(entry.getKey());
            List<BakedQuad> quads = split ? BrokenIconGeometry.splitAll(entry.getValue(), caps, pattern) : entry.getValue();
            // Item rendering does not cull by block adjacency. Emitting all sides as unculled
            // also lets the generated interior faces render exactly once.
            part.original.put(null, List.copyOf(quads));
            groups.put(entry.getKey(), part);
        }
        return new Geometry(List.copyOf(sources), List.copyOf(groups.values()));
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        List<RenderType> original = delegate.getRenderTypes(stack, fabulous);
        return texture == null ? original : original.stream().map(type -> WearRenderType.wrap(type, texture, group.sprite)).toList();
    }

    @Override public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return delegate.isGui3d(); }
    @Override public boolean usesBlockLight() { return delegate.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return delegate.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return delegate.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return delegate.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return delegate.getOverrides(); }

    private record CacheKey(BakedModel model, WearPattern pattern, boolean split, boolean caps) {}
    private record SourceQuad(Direction side, BakedQuad quad, int vertexHash) {}
    private record Geometry(List<SourceQuad> sources, List<Group> groups) {}

    private static final class Group {
        final TextureAtlasSprite sprite;
        final Map<Direction, List<BakedQuad>> original = new HashMap<>();

        Group(TextureAtlasSprite sprite) {
            this.sprite = sprite;
        }
    }
}
