package com.cappleapple.brokennotshattered.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.IQuadTransformer;

/** Produces two separated, texture-preserving halves from ordinary baked item-model quads. */
@OnlyIn(Dist.CLIENT)
final class BrokenIconGeometry {
    static final float CUT_SLOPE = 0.18F;
    static final float HALF_GAP = 1.0F / 32.0F;

    private static final float EPSILON = 1.0E-6F;
    private static final int STRIDE = IQuadTransformer.STRIDE;

    private BrokenIconGeometry() {
    }

    static List<BakedQuad> splitAll(List<BakedQuad> quads, boolean addInteriorCaps) {
        List<BakedQuad> split = new ArrayList<>(quads.size() * 2);
        for (BakedQuad quad : quads) {
            int[] originalVertices = quad.getVertices();
            List<int[]> splitVertices = splitVertexData(originalVertices);
            if (splitVertices.size() == 1 && splitVertices.getFirst() == originalVertices) {
                split.add(quad);
                continue;
            }

            for (int[] vertices : splitVertices) {
                split.add(new BakedQuad(
                    vertices,
                    quad.getTintIndex(),
                    quad.getDirection(),
                    quad.getSprite(),
                    quad.isShade(),
                    quad.hasAmbientOcclusion()
                ));
            }
        }
        if (addInteriorCaps) {
            addInteriorCaps(split, quads);
        }
        return split;
    }

    private static void addInteriorCaps(List<BakedQuad> result, List<BakedQuad> originalQuads) {
        Set<LayerKey> cappedLayers = new HashSet<>();
        for (BakedQuad front : originalQuads) {
            if (!isGeneratedFrontFace(front)) {
                continue;
            }

            LayerKey layer = new LayerKey(front.getSprite(), front.getTintIndex());
            if (!cappedLayers.add(layer)) {
                continue;
            }

            float[] depth = findDepth(originalQuads, layer);
            if (depth[1] - depth[0] <= EPSILON) {
                continue;
            }

            TextureAtlasSprite sprite = front.getSprite();
            Vertex style = Vertex.read(front.getVertices(), 0);
            for (CapData cap : createCapVertexData(
                sprite.contents().width(),
                sprite.contents().height(),
                depth[0],
                depth[1],
                style.color(),
                style.light(),
                sprite.getU0(),
                sprite.getU1(),
                sprite.getV0(),
                sprite.getV1()
            )) {
                result.add(new BakedQuad(
                    cap.vertices(),
                    front.getTintIndex(),
                    cap.direction(),
                    sprite,
                    front.isShade(),
                    front.hasAmbientOcclusion()
                ));
            }
        }
    }

    private static boolean isGeneratedFrontFace(BakedQuad quad) {
        if (quad.getDirection() != Direction.SOUTH || quad.getVertices().length != STRIDE * 4) {
            return false;
        }

        Bounds bounds = bounds(quad.getVertices());
        return bounds.maxX() - bounds.minX() >= 1.0F - EPSILON
            && bounds.maxY() - bounds.minY() >= 1.0F - EPSILON
            && bounds.maxZ() - bounds.minZ() <= EPSILON;
    }

    private static float[] findDepth(List<BakedQuad> quads, LayerKey layer) {
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (BakedQuad quad : quads) {
            if (quad.getSprite() != layer.sprite() || quad.getTintIndex() != layer.tintIndex()) {
                continue;
            }
            Bounds bounds = bounds(quad.getVertices());
            minZ = Math.min(minZ, bounds.minZ());
            maxZ = Math.max(maxZ, bounds.maxZ());
        }
        return new float[]{minZ, maxZ};
    }

    private static Bounds bounds(int[] vertices) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (int offset = 0; offset < vertices.length; offset += STRIDE) {
            float x = Float.intBitsToFloat(vertices[offset]);
            float y = Float.intBitsToFloat(vertices[offset + 1]);
            float z = Float.intBitsToFloat(vertices[offset + 2]);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    static List<CapData> createCapVertexData(
        int textureWidth,
        int textureHeight,
        float minZ,
        float maxZ,
        int color,
        int light,
        float minU,
        float maxU,
        float minV,
        float maxV
    ) {
        if (textureWidth <= 0 || textureHeight <= 0 || maxZ - minZ <= EPSILON) {
            return List.of();
        }

        List<CapData> caps = new ArrayList<>(textureHeight * 2);
        int leftNormal = packNormal(1.0F, -CUT_SLOPE, 0.0F);
        int rightNormal = packNormal(-1.0F, CUT_SLOPE, 0.0F);
        for (int row = 0; row < textureHeight; row++) {
            float yTop = 1.0F - (float) row / textureHeight;
            float yBottom = 1.0F - (float) (row + 1) / textureHeight;
            float xTop = cutX(yTop);
            float xBottom = cutX(yBottom);
            float cutPixelX = cutX((yTop + yBottom) * 0.5F) * textureWidth;
            int leftPixel = clamp((int) Math.ceil(cutPixelX) - 1, 0, textureWidth - 1);
            int rightPixel = clamp((int) Math.floor(cutPixelX), 0, textureWidth - 1);

            float leftUNear = mapUv((leftPixel + 0.25F) / textureWidth, minU, maxU);
            float leftUFar = mapUv((leftPixel + 0.75F) / textureWidth, minU, maxU);
            float rightUNear = mapUv((rightPixel + 0.25F) / textureWidth, minU, maxU);
            float rightUFar = mapUv((rightPixel + 0.75F) / textureWidth, minU, maxU);
            float vTop = mapUv((float) row / textureHeight, minV, maxV);
            float vBottom = mapUv((float) (row + 1) / textureHeight, minV, maxV);

            caps.add(new CapData(
                packQuad(
                    new Vertex(xBottom - HALF_GAP, yBottom, maxZ, color, leftUFar, vBottom, light, leftNormal),
                    new Vertex(xBottom - HALF_GAP, yBottom, minZ, color, leftUNear, vBottom, light, leftNormal),
                    new Vertex(xTop - HALF_GAP, yTop, minZ, color, leftUNear, vTop, light, leftNormal),
                    new Vertex(xTop - HALF_GAP, yTop, maxZ, color, leftUFar, vTop, light, leftNormal)
                ),
                Direction.EAST
            ));
            caps.add(new CapData(
                packQuad(
                    new Vertex(xBottom + HALF_GAP, yBottom, minZ, color, rightUNear, vBottom, light, rightNormal),
                    new Vertex(xBottom + HALF_GAP, yBottom, maxZ, color, rightUFar, vBottom, light, rightNormal),
                    new Vertex(xTop + HALF_GAP, yTop, maxZ, color, rightUFar, vTop, light, rightNormal),
                    new Vertex(xTop + HALF_GAP, yTop, minZ, color, rightUNear, vTop, light, rightNormal)
                ),
                Direction.WEST
            ));
        }
        return caps;
    }

    static List<int[]> splitVertexData(int[] source) {
        if (source.length != STRIDE * 4) {
            return List.of(source);
        }

        List<Vertex> original = new ArrayList<>(4);
        for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
            original.add(Vertex.read(source, vertexIndex * STRIDE));
        }

        List<int[]> result = new ArrayList<>(4);
        addPolygon(result, translate(clip(original, -1), -HALF_GAP));
        addPolygon(result, translate(clip(original, 1), HALF_GAP));
        return result.isEmpty() ? List.of(source) : result;
    }

    private static List<Vertex> clip(List<Vertex> vertices, int side) {
        List<Vertex> output = new ArrayList<>(vertices.size() + 1);
        Vertex previous = vertices.getLast();
        float previousDistance = distance(previous);
        boolean previousInside = side * previousDistance >= -EPSILON;

        for (Vertex current : vertices) {
            float currentDistance = distance(current);
            boolean currentInside = side * currentDistance >= -EPSILON;
            if (currentInside != previousInside) {
                float interpolation = previousDistance / (previousDistance - currentDistance);
                output.add(previous.interpolate(current, interpolation));
            }
            if (currentInside) {
                output.add(current);
            }

            previous = current;
            previousDistance = currentDistance;
            previousInside = currentInside;
        }
        return output;
    }

    private static float distance(Vertex vertex) {
        return vertex.x() - cutX(vertex.y());
    }

    private static float cutX(float y) {
        return 0.5F + CUT_SLOPE * (y - 0.5F);
    }

    private static List<Vertex> translate(List<Vertex> vertices, float xOffset) {
        return vertices.stream().map(vertex -> vertex.translateX(xOffset)).toList();
    }

    private static void addPolygon(List<int[]> result, List<Vertex> polygon) {
        if (polygon.size() < 3) {
            return;
        }
        if (polygon.size() == 4) {
            result.add(packQuad(polygon.get(0), polygon.get(1), polygon.get(2), polygon.get(3)));
            return;
        }

        Vertex first = polygon.getFirst();
        for (int index = 1; index < polygon.size() - 1; index++) {
            Vertex second = polygon.get(index);
            Vertex third = polygon.get(index + 1);
            result.add(packQuad(first, second, third, third));
        }
    }

    private static int[] packQuad(Vertex first, Vertex second, Vertex third, Vertex fourth) {
        int[] packed = new int[STRIDE * 4];
        first.write(packed, 0);
        second.write(packed, STRIDE);
        third.write(packed, STRIDE * 2);
        fourth.write(packed, STRIDE * 3);
        return packed;
    }

    private record Vertex(float x, float y, float z, int color, float u, float v, int light, int normal) {
        static Vertex read(int[] packed, int offset) {
            return new Vertex(
                Float.intBitsToFloat(packed[offset]),
                Float.intBitsToFloat(packed[offset + 1]),
                Float.intBitsToFloat(packed[offset + 2]),
                packed[offset + 3],
                Float.intBitsToFloat(packed[offset + 4]),
                Float.intBitsToFloat(packed[offset + 5]),
                packed[offset + 6],
                packed[offset + 7]
            );
        }

        Vertex interpolate(Vertex other, float amount) {
            return new Vertex(
                lerp(x, other.x, amount),
                lerp(y, other.y, amount),
                lerp(z, other.z, amount),
                lerpPackedBytes(color, other.color, amount),
                lerp(u, other.u, amount),
                lerp(v, other.v, amount),
                lerpPackedShorts(light, other.light, amount),
                normal
            );
        }

        Vertex translateX(float amount) {
            return new Vertex(x + amount, y, z, color, u, v, light, normal);
        }

        void write(int[] target, int offset) {
            target[offset] = Float.floatToRawIntBits(x);
            target[offset + 1] = Float.floatToRawIntBits(y);
            target[offset + 2] = Float.floatToRawIntBits(z);
            target[offset + 3] = color;
            target[offset + 4] = Float.floatToRawIntBits(u);
            target[offset + 5] = Float.floatToRawIntBits(v);
            target[offset + 6] = light;
            target[offset + 7] = normal;
        }
    }

    private static float lerp(float first, float second, float amount) {
        return first + (second - first) * amount;
    }

    private static int lerpPackedBytes(int first, int second, float amount) {
        int result = 0;
        for (int shift = 0; shift < 32; shift += 8) {
            int value = Math.round(lerp((first >>> shift) & 0xFF, (second >>> shift) & 0xFF, amount));
            result |= (value & 0xFF) << shift;
        }
        return result;
    }

    private static int lerpPackedShorts(int first, int second, float amount) {
        int low = Math.round(lerp(first & 0xFFFF, second & 0xFFFF, amount)) & 0xFFFF;
        int high = Math.round(lerp((first >>> 16) & 0xFFFF, (second >>> 16) & 0xFFFF, amount)) & 0xFFFF;
        return low | high << 16;
    }

    private static float mapUv(float value, float minimum, float maximum) {
        return minimum + (maximum - minimum) * value;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static int packNormal(float x, float y, float z) {
        float inverseLength = 1.0F / (float) Math.sqrt(x * x + y * y + z * z);
        int packedX = Math.round(x * inverseLength * 127.0F) & 0xFF;
        int packedY = Math.round(y * inverseLength * 127.0F) & 0xFF;
        int packedZ = Math.round(z * inverseLength * 127.0F) & 0xFF;
        return packedX | packedY << 8 | packedZ << 16;
    }

    record CapData(int[] vertices, Direction direction) {
    }

    private record LayerKey(TextureAtlasSprite sprite, int tintIndex) {
    }

    private record Bounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    }
}
