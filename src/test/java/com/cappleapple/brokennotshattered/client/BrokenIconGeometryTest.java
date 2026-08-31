package com.cappleapple.brokennotshattered.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.junit.jupiter.api.Test;

class BrokenIconGeometryTest {
    private static final int STRIDE = 8;

    @Test
    void splitsAQuadIntoSeparatedTexturePreservingHalves() {
        int[] source = quad(
            vertex(0.0F, 0.0F, 0.0F, 0.0F),
            vertex(1.0F, 0.0F, 1.0F, 0.0F),
            vertex(1.0F, 1.0F, 1.0F, 1.0F),
            vertex(0.0F, 1.0F, 0.0F, 1.0F)
        );

        List<int[]> split = BrokenIconGeometry.splitVertexData(source);

        assertEquals(2, split.size());
        for (int[] half : split) {
            assertEquals(STRIDE * 4, half.length);
        }

        assertEntirelyOnSide(split.get(0), -1);
        assertEntirelyOnSide(split.get(1), 1);
        assertTrue(hasInterpolatedCutUv(split.get(0)), "left half did not retain interpolated cut UVs");
        assertTrue(hasInterpolatedCutUv(split.get(1)), "right half did not retain interpolated cut UVs");
    }

    @Test
    void leavesUnexpectedVertexFormatsUntouched() {
        int[] malformed = new int[7];
        List<int[]> result = BrokenIconGeometry.splitVertexData(malformed);

        assertEquals(1, result.size());
        assertSame(malformed, result.getFirst());
    }

    @Test
    void buildsSolidSeparatedFacesAcrossTheFlatModelDepth() {
        float minZ = 7.5F / 16.0F;
        float maxZ = 8.5F / 16.0F;
        List<BrokenIconGeometry.CapData> caps = BrokenIconGeometry.createCapVertexData(
            16,
            16,
            minZ,
            maxZ,
            0xFFFFFFFF,
            0,
            0.0F,
            1.0F,
            0.0F,
            1.0F
        );

        assertEquals(32, caps.size());
        for (int index = 0; index < caps.size(); index++) {
            BrokenIconGeometry.CapData cap = caps.get(index);
            int side = cap.direction() == Direction.EAST ? -1 : 1;
            assertTrue(cap.direction() == Direction.EAST || cap.direction() == Direction.WEST);
            assertEntirelyOnSide(cap.vertices(), side);
            assertSpansDepth(cap.vertices(), minZ, maxZ);
        }
    }

    @Test
    void appendsSolidCapsOnlyWhenHeldOrDroppedRenderingRequestsThem() {
        NativeImage image = new NativeImage(16, 16, false);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setPixelRGBA(x, y, 0xFFFFFFFF);
            }
        }

        try (SpriteContents contents = new SpriteContents(
            ResourceLocation.fromNamespaceAndPath("broken_not_shattered", "test_tool"),
            new FrameSize(16, 16),
            image,
            ResourceMetadata.EMPTY
        )) {
            TextureAtlasSprite sprite = new TestSprite(contents);
            List<BakedQuad> flatModel = List.of(
                face(sprite, Direction.SOUTH, 8.5F / 16.0F),
                face(sprite, Direction.NORTH, 7.5F / 16.0F)
            );

            List<BakedQuad> withoutCaps = BrokenIconGeometry.splitAll(flatModel, false);
            List<BakedQuad> withCaps = BrokenIconGeometry.splitAll(flatModel, true);

            assertEquals(withoutCaps.size() + 32, withCaps.size());
            assertEquals(
                16,
                withCaps.subList(withoutCaps.size(), withCaps.size())
                    .stream()
                    .filter(quad -> quad.getDirection() == Direction.EAST)
                    .count()
            );
            assertEquals(
                16,
                withCaps.subList(withoutCaps.size(), withCaps.size())
                    .stream()
                    .filter(quad -> quad.getDirection() == Direction.WEST)
                    .count()
            );
        }
    }

    private static void assertEntirelyOnSide(int[] quad, int side) {
        for (int offset = 0; offset < quad.length; offset += STRIDE) {
            float x = Float.intBitsToFloat(quad[offset]);
            float y = Float.intBitsToFloat(quad[offset + 1]);
            float cutX = 0.5F + BrokenIconGeometry.CUT_SLOPE * (y - 0.5F);
            float signedDistance = x - cutX;
            assertTrue(
                side * signedDistance >= BrokenIconGeometry.HALF_GAP - 1.0E-5F,
                "vertex crossed the separated cut: x=" + x + ", y=" + y
            );
        }
    }

    private static boolean hasInterpolatedCutUv(int[] quad) {
        for (int offset = 0; offset < quad.length; offset += STRIDE) {
            float u = Float.intBitsToFloat(quad[offset + 4]);
            if (u > 0.0F && u < 1.0F) {
                return true;
            }
        }
        return false;
    }

    private static void assertSpansDepth(int[] quad, float expectedMin, float expectedMax) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (int offset = 0; offset < quad.length; offset += STRIDE) {
            float z = Float.intBitsToFloat(quad[offset + 2]);
            min = Math.min(min, z);
            max = Math.max(max, z);
        }
        assertEquals(expectedMin, min);
        assertEquals(expectedMax, max);
    }

    private static int[] quad(int[]... vertices) {
        int[] packed = new int[STRIDE * 4];
        for (int index = 0; index < vertices.length; index++) {
            System.arraycopy(vertices[index], 0, packed, index * STRIDE, STRIDE);
        }
        return packed;
    }

    private static int[] vertex(float x, float y, float u, float v) {
        return vertex(x, y, 0.5F, u, v);
    }

    private static int[] vertex(float x, float y, float z, float u, float v) {
        return new int[]{
            Float.floatToRawIntBits(x),
            Float.floatToRawIntBits(y),
            Float.floatToRawIntBits(z),
            0xFFFFFFFF,
            Float.floatToRawIntBits(u),
            Float.floatToRawIntBits(v),
            0,
            0
        };
    }

    private static BakedQuad face(TextureAtlasSprite sprite, Direction direction, float z) {
        return new BakedQuad(
            quad(
                vertex(0.0F, 0.0F, z, sprite.getU0(), sprite.getV1()),
                vertex(1.0F, 0.0F, z, sprite.getU1(), sprite.getV1()),
                vertex(1.0F, 1.0F, z, sprite.getU1(), sprite.getV0()),
                vertex(0.0F, 1.0F, z, sprite.getU0(), sprite.getV0())
            ),
            -1,
            direction,
            sprite,
            true,
            true
        );
    }

    private static final class TestSprite extends TextureAtlasSprite {
        private TestSprite(SpriteContents contents) {
            super(
                ResourceLocation.fromNamespaceAndPath("broken_not_shattered", "test_atlas"),
                contents,
                16,
                16,
                0,
                0
            );
        }
    }
}
