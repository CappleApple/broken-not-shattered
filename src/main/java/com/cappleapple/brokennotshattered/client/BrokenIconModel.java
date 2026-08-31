package com.cappleapple.brokennotshattered.client;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Delegates a resolved item model while replacing its quads with a procedurally split icon. */
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("deprecation")
public final class BrokenIconModel implements BakedModel {
    private final BakedModel delegate;
    private final boolean addInteriorCaps;

    private BrokenIconModel(BakedModel delegate, boolean addInteriorCaps) {
        this.delegate = delegate;
        this.addInteriorCaps = addInteriorCaps;
    }

    public static BakedModel wrap(BakedModel model, boolean addInteriorCaps) {
        if (model instanceof BrokenIconModel brokenModel) {
            if (brokenModel.addInteriorCaps == addInteriorCaps) {
                return model;
            }
            model = brokenModel.delegate;
        }
        return new BrokenIconModel(model, addInteriorCaps);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return BrokenIconGeometry.splitAll(delegate.getQuads(state, side, random), addInteriorCaps);
    }

    @Override
    public List<BakedQuad> getQuads(
        @Nullable BlockState state,
        @Nullable Direction side,
        RandomSource random,
        ModelData modelData,
        @Nullable RenderType renderType
    ) {
        return BrokenIconGeometry.splitAll(delegate.getQuads(state, side, random, modelData, renderType), addInteriorCaps);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
        return delegate.getRenderPasses(stack, fabulous)
            .stream()
            .map(model -> BrokenIconModel.wrap(model, addInteriorCaps))
            .toList();
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        return delegate.getRenderTypes(stack, fabulous);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return delegate.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return delegate.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return delegate.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return delegate.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return delegate.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return delegate.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return delegate.getOverrides();
    }
}
