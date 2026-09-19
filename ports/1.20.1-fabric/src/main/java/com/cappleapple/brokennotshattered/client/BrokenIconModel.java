package com.cappleapple.brokennotshattered.client;

import com.google.common.collect.MapMaker;
import java.util.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Resolved vanilla item model with bounded per-stack fracture geometry caching. */
public final class BrokenIconModel implements BakedModel {
    private static final Map<ItemStack,Map<Key,Geometry>> CACHE=new MapMaker().weakKeys().makeMap();
    private final BakedModel delegate;
    private final List<BakedQuad> quads;
    private BrokenIconModel(BakedModel model,List<BakedQuad> quads){this.delegate=model;this.quads=quads;}
    public static BakedModel wrap(BakedModel model,ItemStack owner,WearPattern pattern,boolean split,boolean caps){
        var source=new ArrayList<Source>();var random=RandomSource.create(42);
        for(Direction face:Direction.values()){
            random.setSeed(42);
            for(var quad:model.getQuads(null,face,random))source.add(new Source(quad,Arrays.hashCode(quad.getVertices())));
        }
        random.setSeed(42);
        for(var quad:model.getQuads(null,null,random))source.add(new Source(quad,Arrays.hashCode(quad.getVertices())));
        var key=new Key(model,pattern,split,caps);
        var cache=CACHE.computeIfAbsent(owner,ignored->new LinkedHashMap<>());
        var geometry=cache.get(key);
        if(geometry==null||!geometry.source.equals(source)){
            var quads=source.stream().map(Source::quad).toList();
            geometry=new Geometry(List.copyOf(source),split?List.copyOf(BrokenIconGeometry.splitAll(quads,caps,pattern)):quads);
            if(cache.size()>=8)cache.clear();cache.put(key,geometry);
        }
        return new BrokenIconModel(model,geometry.quads);
    }
    static void clearCache(){CACHE.clear();}
    public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random){return side==null?quads:List.of();}
    public boolean useAmbientOcclusion(){return delegate.useAmbientOcclusion();}
    public boolean isGui3d(){return delegate.isGui3d();}
    public boolean usesBlockLight(){return delegate.usesBlockLight();}
    public boolean isCustomRenderer(){return delegate.isCustomRenderer();}
    public TextureAtlasSprite getParticleIcon(){return delegate.getParticleIcon();}
    public ItemTransforms getTransforms(){return delegate.getTransforms();}
    public ItemOverrides getOverrides(){return delegate.getOverrides();}
    private record Key(BakedModel model,WearPattern pattern,boolean split,boolean caps){}
    private record Source(BakedQuad quad,int vertexHash){}
    private record Geometry(List<Source> source,List<BakedQuad> quads){}
}
