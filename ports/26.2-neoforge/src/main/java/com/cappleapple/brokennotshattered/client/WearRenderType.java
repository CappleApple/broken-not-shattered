package com.cappleapple.brokennotshattered.client;
import com.cappleapple.brokennotshattered.mixin.RenderTypeAccessor;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
public final class WearRenderType {
    private static final Map<Key, RenderType> TYPES = new LinkedHashMap<>();
    private static final java.util.Set<RenderType> WORN = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    public interface Setup { RenderSetup bns$withTexture(Identifier texture); }
    public static RenderType wrap(RenderType original, Identifier texture) {
        Key key = new Key(original, texture);
        RenderType cached = TYPES.get(key);
        if (cached != null) return cached;
        if (TYPES.size() >= 1024) TYPES.clear();
        RenderSetup setup = ((RenderTypeAccessor)original).bns$setup();
        RenderType result = RenderType.create("broken_not_shattered_wear", ((Setup)(Object)setup).bns$withTexture(texture));
        TYPES.put(key, result); WORN.add(result);
        return result;
    }
    public static boolean isWorn(RenderType type) { return WORN.contains(type); }
    public static void clear() { TYPES.clear(); WORN.clear(); }
    private record Key(RenderType original, Identifier texture) {}
}
