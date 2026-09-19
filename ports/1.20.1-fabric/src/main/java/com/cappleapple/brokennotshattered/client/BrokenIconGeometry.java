package com.cappleapple.brokennotshattered.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/** Follows complete jagged fractures, preserving UVs and capping flat item cuts at every turn. */
final class BrokenIconGeometry {
    static final float HALF_GAP = 1.0F / 64.0F;

    private static final float EPSILON = 1.0E-6F;
    private static final int STRIDE = 8;

    private BrokenIconGeometry() {
    }

    static List<BakedQuad> splitAll(List<BakedQuad> quads, boolean addInteriorCaps, WearPattern pattern) {
        List<WearPattern.Path> seams = pattern.seams();
        if (seams.isEmpty()) return quads;
        List<BakedQuad> split = new ArrayList<>(quads.size() * 2);
        for (BakedQuad quad : quads) {
            int[] originalVertices = quad.getVertices();
            List<int[]> splitVertices = splitVertexData(originalVertices, seams);
            if (splitVertices.size() == 1 && splitVertices.get(0) == originalVertices) {
                split.add(quad);
                continue;
            }

            for (int[] vertices : splitVertices) {
                split.add(new BakedQuad(
                    vertices,
                    quad.getTintIndex(),
                    quad.getDirection(),
                    quad.getSprite(),
                    quad.isShade()
                ));
            }
        }
        if (addInteriorCaps) {
            addInteriorCaps(split, quads, pattern);
        }
        return split;
    }

    private static void addInteriorCaps(List<BakedQuad> result, List<BakedQuad> originalQuads, WearPattern pattern) {
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
                sprite.getV1(),
                pattern
            )) {
                result.add(new BakedQuad(
                    cap.vertices(),
                    front.getTintIndex(),
                    cap.direction(),
                    sprite,
                    front.isShade()
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
        int textureWidth, int textureHeight, float minZ, float maxZ, int color, int light,
        float minU, float maxU, float minV, float maxV, WearPattern pattern
    ) {
        if (textureWidth <= 0 || textureHeight <= 0 || maxZ - minZ <= EPSILON) {
            return List.of();
        }

        List<CapData> caps = new ArrayList<>(textureHeight * 2);
        List<WearPattern.Path> seams = pattern.seams();
        for (int cut = 0; cut < seams.size(); cut++) {
            WearPattern.Path seam = seams.get(cut);
            float leftOffset = bandOffset(cut, seams.size());
            float rightOffset = bandOffset(cut + 1, seams.size());
            for (int segment = 1; segment < seam.points().size(); segment++) {
                WearPattern.Point from = seam.points().get(segment - 1);
                WearPattern.Point to = seam.points().get(segment);
                float slope = (to.x() - from.x()) / (to.y() - from.y());
                int leftNormal = packNormal(1, -slope, 0);
                int rightNormal = packNormal(-1, slope, 0);
                int firstRow = Math.max(0, (int) Math.floor((1 - to.y()) * textureHeight));
                int lastRow = Math.min(textureHeight, (int) Math.ceil((1 - from.y()) * textureHeight));
                for (int row = firstRow; row < lastRow; row++) {
                    float yTop = Math.min(to.y(), 1.0F - (float) row / textureHeight);
                    float yBottom = Math.max(from.y(), 1.0F - (float) (row + 1) / textureHeight);
                    if (Math.abs(slope) > EPSILON) {
                        float a = from.y() - from.x() / slope;
                        float b = from.y() + (1 - from.x()) / slope;
                        yBottom = Math.max(yBottom, Math.min(a, b));
                        yTop = Math.min(yTop, Math.max(a, b));
                    }
                    if (yTop - yBottom <= EPSILON) continue;
                    float xTop = seam.xAt(yTop);
                    float xBottom = seam.xAt(yBottom);
                    float cutPixelX = seam.xAt((yTop + yBottom) * 0.5F) * textureWidth;
                    int leftPixel = clamp((int) Math.ceil(cutPixelX) - 1, 0, textureWidth - 1);
                    int rightPixel = clamp((int) Math.floor(cutPixelX), 0, textureWidth - 1);

                    float leftUNear = mapUv((leftPixel + 0.25F) / textureWidth, minU, maxU);
                    float leftUFar = mapUv((leftPixel + 0.75F) / textureWidth, minU, maxU);
                    float rightUNear = mapUv((rightPixel + 0.25F) / textureWidth, minU, maxU);
                    float rightUFar = mapUv((rightPixel + 0.75F) / textureWidth, minU, maxU);
                    float vTop = mapUv(1 - yTop, minV, maxV);
                    float vBottom = mapUv(1 - yBottom, minV, maxV);

                    caps.add(new CapData(
                        packQuad(
                            new Vertex(xBottom + leftOffset, yBottom, maxZ, color, leftUFar, vBottom, light, leftNormal),
                            new Vertex(xBottom + leftOffset, yBottom, minZ, color, leftUNear, vBottom, light, leftNormal),
                            new Vertex(xTop + leftOffset, yTop, minZ, color, leftUNear, vTop, light, leftNormal),
                            new Vertex(xTop + leftOffset, yTop, maxZ, color, leftUFar, vTop, light, leftNormal)
                        ),
                        Direction.EAST
                    ));
                    caps.add(new CapData(
                        packQuad(
                            new Vertex(xBottom + rightOffset, yBottom, minZ, color, rightUNear, vBottom, light, rightNormal),
                            new Vertex(xBottom + rightOffset, yBottom, maxZ, color, rightUFar, vBottom, light, rightNormal),
                            new Vertex(xTop + rightOffset, yTop, maxZ, color, rightUFar, vTop, light, rightNormal),
                            new Vertex(xTop + rightOffset, yTop, minZ, color, rightUNear, vTop, light, rightNormal)
                        ),
                        Direction.WEST
                    ));
                }
            }
        }
        return caps;
    }

    static List<int[]> splitVertexData(int[] source, WearPattern pattern) {
        return splitVertexData(source, pattern.seams());
    }

    private static List<int[]> splitVertexData(int[] source, List<WearPattern.Path> seams) {
        if (seams.isEmpty() || source.length != STRIDE * 4) {
            return List.of(source);
        }

        List<Vertex> original = new ArrayList<>(4);
        for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
            original.add(Vertex.read(source, vertexIndex * STRIDE));
        }

        // Clip at every bend before clipping sideways. Extending one diagonal over the whole
        // quad would erase the turns and reopen a straight gap through the texture.
        Bounds bounds = bounds(source);
        TreeSet<Float> breaks = new TreeSet<>();
        breaks.add(bounds.minY());
        breaks.add(bounds.maxY());
        for (WearPattern.Path seam : seams) for (WearPattern.Point point : seam.points()) {
            if (point.y() > bounds.minY() + EPSILON && point.y() < bounds.maxY() - EPSILON) {
                breaks.add(point.y());
            }
        }
        List<Float> levels = List.copyOf(breaks);
        List<int[]> result = new ArrayList<>();
        for (int row = 0; row < Math.max(1, levels.size() - 1); row++) {
            List<Vertex> strip = original;
            if (levels.size() > 1) {
                float bottom = levels.get(row);
                float top = levels.get(row + 1);
                strip = clip(strip, 1, vertex -> vertex.y() - bottom);
                strip = clip(strip, -1, vertex -> vertex.y() - top);
            }
            for (int band = 0; band <= seams.size(); band++) {
                List<Vertex> polygon = strip;
                if (band > 0) {
                    WearPattern.Path left = seams.get(band - 1);
                    polygon = clip(polygon, 1, vertex -> vertex.x() - left.xAt(vertex.y()));
                }
                if (band < seams.size()) {
                    WearPattern.Path right = seams.get(band);
                    polygon = clip(polygon, -1, vertex -> vertex.x() - right.xAt(vertex.y()));
                }
                addPolygon(result, translate(polygon, bandOffset(band, seams.size())));
            }
        }
        return result.isEmpty() ? List.of(source) : result;
    }

    private static List<Vertex> clip(List<Vertex> vertices, int side, ToDoubleFunction<Vertex> distance) {
        if (vertices.isEmpty()) return vertices;
        List<Vertex> output = new ArrayList<>(vertices.size() + 1);
        Vertex previous = vertices.get(vertices.size()-1);
        double previousDistance = distance.applyAsDouble(previous);
        boolean previousInside = side * previousDistance >= -EPSILON;

        for (Vertex current : vertices) {
            double currentDistance = distance.applyAsDouble(current);
            boolean currentInside = side * currentDistance >= -EPSILON;
            if (currentInside != previousInside) {
                float interpolation = (float) (previousDistance / (previousDistance - currentDistance));
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

    private static float bandOffset(int band, int seamCount) {
        return (band - seamCount * 0.5F) * (2 * HALF_GAP);
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

        Vertex first = polygon.get(0);
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
