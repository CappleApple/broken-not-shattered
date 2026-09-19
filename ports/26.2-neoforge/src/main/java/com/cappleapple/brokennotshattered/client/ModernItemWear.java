package com.cappleapple.brokennotshattered.client;

import com.cappleapple.brokennotshattered.mixin.ItemRenderStateAccessor;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import net.minecraft.client.model.geom.builders.UVPair;

/** Changes resolved item layers; foil keeps the original atlas coordinates. */
public final class ModernItemWear {
    private static final com.google.common.cache.Cache<GeometryKey, List<BakedQuad>> GEOMETRY =
        com.google.common.cache.CacheBuilder.newBuilder().maximumWeight(50000)
            .weigher((GeometryKey key, List<BakedQuad> value) -> Math.max(1, value.size())).build();
    private record GeometryKey(List<BakedQuad> source, WearPattern pattern, boolean caps) {}
    private ModernItemWear() {}
    public static void clear() { GEOMETRY.invalidateAll(); WearRenderType.clear(); }

    public static void apply(ItemStackRenderState state, ItemStack stack, ItemDisplayContext context) {
        WearPattern pattern = BrokenAppearance.pattern(stack);
        if (pattern == null) return;
        ItemRenderStateAccessor access = (ItemRenderStateAccessor) state;
        for (int i = 0; i < access.bns$count(); i++) {
            List<BakedQuad> quads = access.bns$layers()[i].prepareQuadList();
            if (quads.isEmpty()) continue;
            boolean flat = quads.stream().allMatch(q -> {
                for (int v = 0; v < 4; v++) if (Math.abs(q.position(v).z() - 0.5f) > 0.063f) return false;
                return true;
            });
            List<BakedQuad> geometry = List.copyOf(quads);
            if (context == ItemDisplayContext.GUI || flat) {
                GeometryKey key = new GeometryKey(geometry, pattern, context != ItemDisplayContext.GUI);
                geometry = GEOMETRY.asMap().computeIfAbsent(key,
                    value -> List.copyOf(BrokenIconGeometry.splitAll(value.source(), value.caps(), value.pattern())));
            }
            List<BakedQuad> output = new ArrayList<>(geometry.size());
            for (BakedQuad q : geometry) {
                var m = q.materialInfo();
                var texture = WornTextures.sprite(m.sprite(), pattern);
                if (texture == null) { output.add(q); continue; }
                var material = new BakedQuad.MaterialInfo(m.sprite(), m.layer(),
                    WearRenderType.wrap(m.itemRenderType(), texture), m.tintIndex(), m.shade(), m.lightEmission(), m.ambientOcclusion());
                output.add(new BakedQuad(q.position0(), q.position1(), q.position2(), q.position3(),
                    q.packedUV0(), q.packedUV1(), q.packedUV2(), q.packedUV3(), q.direction(), material, q.bakedNormals(), q.bakedColors()));
            }
            quads.clear();
            quads.addAll(output);
        }
        // The GUI caches model identities; each saved pattern needs its own cache entry.
        state.appendModelIdentityElement(pattern);
    }

    public static BakedQuad baseTextureQuad(BakedQuad q) {
        if (!WearRenderType.isWorn(q.materialInfo().itemRenderType())) return q;
        var sprite = q.materialInfo().sprite();
        long[] uv = new long[4];
        for (int v = 0; v < 4; v++) {
            float u = UVPair.unpackU(q.packedUV(v));
            float w = UVPair.unpackV(q.packedUV(v));
            uv[v] = pack((u - sprite.getU0()) / (sprite.getU1() - sprite.getU0()),
                (w - sprite.getV0()) / (sprite.getV1() - sprite.getV0()));
        }
        return new BakedQuad(q.position0(), q.position1(), q.position2(), q.position3(), uv[0], uv[1], uv[2], uv[3],
            q.direction(), q.materialInfo(), q.bakedNormals(), q.bakedColors());
    }

    static int[] vertices(BakedQuad quad) {
        int[] data = new int[32];
        for (int i = 0; i < 4; i++) {
            int o = i * 8;
            data[o] = Float.floatToRawIntBits(quad.position(i).x());
            data[o+1] = Float.floatToRawIntBits(quad.position(i).y());
            data[o+2] = Float.floatToRawIntBits(quad.position(i).z());
            data[o+3] = quad.bakedColors().color(i);
            data[o+4] = Float.floatToRawIntBits(UVPair.unpackU(quad.packedUV(i)));
            data[o+5] = Float.floatToRawIntBits(UVPair.unpackV(quad.packedUV(i)));
            data[o+7] = quad.bakedNormals().normal(i);
        }
        return data;
    }

    static BakedQuad quad(int[] vertices, Direction direction, BakedQuad source) {
        Vector3f[] pos = new Vector3f[4];
        long[] uv = new long[4];
        for (int i = 0; i < 4; i++) {
            int o = i*8;
            pos[i] = new Vector3f(Float.intBitsToFloat(vertices[o]), Float.intBitsToFloat(vertices[o+1]), Float.intBitsToFloat(vertices[o+2]));
            uv[i] = UVPair.pack(Float.intBitsToFloat(vertices[o+4]), Float.intBitsToFloat(vertices[o+5]));
        }
        return new BakedQuad(pos[0], pos[1], pos[2], pos[3], uv[0], uv[1], uv[2], uv[3], direction,
            source.materialInfo(), BakedNormals.of(vertices[7], vertices[15], vertices[23], vertices[31]),
            BakedColors.of(vertices[3], vertices[11], vertices[19], vertices[27]));
    }

    private static long pack(float u, float v) {
        return UVPair.pack(u, v);
    }
}
