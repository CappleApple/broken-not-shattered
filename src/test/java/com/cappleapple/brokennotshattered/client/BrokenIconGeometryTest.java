package com.cappleapple.brokennotshattered.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
    private static final WearPattern SINGLE_CUT = straightCuts(0.18F, 0.5F);

    @Test
    void multipleBreaksPreserveAreaAndCapEverySeam() {
        int[] source = quad(vertex(0, 0, 0, 0), vertex(1, 0, 1, 0), vertex(1, 1, 1, 1), vertex(0, 1, 0, 1));
        WearPattern pattern = straightCuts(0.1F, 0.25F, 0.5F, 0.75F);
        List<int[]> pieces = BrokenIconGeometry.splitVertexData(source, pattern);
        assertEquals(4, pieces.size());
        double area = 0;
        for (int[] piece : pieces) {
            double twiceArea = 0;
            for (int i = 0; i < 4; i++) {
                int a = i * STRIDE;
                int b = ((i + 1) % 4) * STRIDE;
                twiceArea += Float.intBitsToFloat(piece[a]) * Float.intBitsToFloat(piece[b + 1])
                    - Float.intBitsToFloat(piece[b]) * Float.intBitsToFloat(piece[a + 1]);
            }
            area += Math.abs(twiceArea) / 2;
        }
        assertEquals(1, area, 0.00001, "separation must neither discard nor duplicate model surface");
        assertEquals(96, BrokenIconGeometry.createCapVertexData(16, 16, 0.45F, 0.55F,
            -1, 0, 0, 1, 0, 1, pattern).size());
    }

    @Test
    void steepCutsNeverGenerateCapsOutsideTheOriginalSurface() {
        WearPattern pattern = straightCuts(0.55F, 0.1F, 0.9F);
        for (var cap : BrokenIconGeometry.createCapVertexData(16, 16, 0.45F, 0.55F, -1, 0, 0, 1, 0, 1, pattern)) {
            for (int offset = 0; offset < cap.vertices().length; offset += STRIDE) {
                float x = Float.intBitsToFloat(cap.vertices()[offset]);
                float y = Float.intBitsToFloat(cap.vertices()[offset + 1]);
                assertTrue(x >= -0.032F && x <= 1.032F);
                assertTrue(y >= 0 && y <= 1);
            }
        }
    }

    @Test
    void splitsAQuadIntoSeparatedTexturePreservingHalves() {
        int[] source = quad(
            vertex(0.0F, 0.0F, 0.0F, 0.0F),
            vertex(1.0F, 0.0F, 1.0F, 0.0F),
            vertex(1.0F, 1.0F, 1.0F, 1.0F),
            vertex(0.0F, 1.0F, 0.0F, 1.0F)
        );

        List<int[]> split = BrokenIconGeometry.splitVertexData(source, SINGLE_CUT);

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
        List<int[]> result = BrokenIconGeometry.splitVertexData(malformed, SINGLE_CUT);

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
            1.0F,
            SINGLE_CUT
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

            List<BakedQuad> withoutCaps = BrokenIconGeometry.splitAll(flatModel, false, SINGLE_CUT);
            List<BakedQuad> withCaps = BrokenIconGeometry.splitAll(flatModel, true, SINGLE_CUT);

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
            float cutX = SINGLE_CUT.seams().getFirst().xAt(y);
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

    @Test
    void jaggedFacesAndCapsFollowEveryTurnWithoutLosingSurfaceOrStretchingUvs() {
        var path = new WearPattern.Path(List.of(new WearPattern.Point(0.3F, 0),
            new WearPattern.Point(0.65F, 0.25F), new WearPattern.Point(0.4F, 0.65F),
            new WearPattern.Point(0.55F, 1)));
        var pattern = new WearPattern(0, List.of(new WearPattern.Crack(path, List.of())), WearSettings.DEFAULT);
        int[] source = quad(vertex(0, 0, 0, 0), vertex(1, 0, 1, 0), vertex(1, 1, 1, 1), vertex(0, 1, 0, 1));
        var pieces = BrokenIconGeometry.splitVertexData(source, pattern);
        assertEquals(6, pieces.size());
        assertEquals(1, surfaceArea(pieces), 0.00001);
        for (int[] piece : pieces) for (int offset = 0; offset < piece.length; offset += STRIDE) {
            float x = Float.intBitsToFloat(piece[offset]);
            float y = Float.intBitsToFloat(piece[offset + 1]);
            float u = Float.intBitsToFloat(piece[offset + 4]);
            float v = Float.intBitsToFloat(piece[offset + 5]);
            assertEquals(BrokenIconGeometry.HALF_GAP, Math.abs(x - u), 0.00001);
            assertEquals(y, v, 0.00001, "splitting must preserve the original UV mapping");
            assertTrue(Math.abs(x - path.xAt(y)) >= BrokenIconGeometry.HALF_GAP - 0.00001);
        }
        Set<Integer> normals = new HashSet<>();
        Set<Float> levels = new HashSet<>();
        for (var cap : BrokenIconGeometry.createCapVertexData(16, 16, 0.45F, 0.55F,
            -1, 0, 0.2F, 0.4F, 0.6F, 0.8F, pattern)) {
            float shift = cap.direction() == Direction.EAST ? -BrokenIconGeometry.HALF_GAP : BrokenIconGeometry.HALF_GAP;
            for (int offset = 0; offset < cap.vertices().length; offset += STRIDE) {
                float x = Float.intBitsToFloat(cap.vertices()[offset]);
                float y = Float.intBitsToFloat(cap.vertices()[offset + 1]);
                float u = Float.intBitsToFloat(cap.vertices()[offset + 4]);
                float v = Float.intBitsToFloat(cap.vertices()[offset + 5]);
                assertEquals(path.xAt(y) + shift, x, 0.00001, "interior walls must meet the jagged front face");
                assertTrue(u >= 0.2F && u <= 0.4F && v >= 0.6F && v <= 0.8F);
                levels.add(y);
                if (cap.direction() == Direction.EAST) normals.add(cap.vertices()[offset + 7]);
            }
            assertSpansDepth(cap.vertices(), 0.45F, 0.55F);
        }
        assertEquals(3, normals.size(), "the interior wall normal must change at each turn");
        assertTrue(levels.containsAll(List.of(0.25F, 0.65F)), "caps must end precisely at path bends");
    }

    @Test
    void partialCracksDoNotSplitTheRestOfTheModel() {
        var path = new WearPattern.Path(List.of(new WearPattern.Point(0.4F, 0.2F),
            new WearPattern.Point(0.6F, 0.4F), new WearPattern.Point(0.5F, 0.7F)));
        var pattern = new WearPattern(0, List.of(new WearPattern.Crack(path, List.of())), WearSettings.DEFAULT);
        int[] source = quad(vertex(0, 0, 0, 0), vertex(1, 0, 1, 0), vertex(1, 1, 1, 1), vertex(0, 1, 0, 1));
        var pieces = BrokenIconGeometry.splitVertexData(source, pattern);
        assertEquals(1, pieces.size());
        assertSame(source, pieces.getFirst());
        assertTrue(BrokenIconGeometry.createCapVertexData(16, 16, 0.45F, 0.55F,
            -1, 0, 0, 1, 0, 1, pattern).isEmpty());
    }

    @Test
    void generatedSeamsPreserveSurfaceAreaAtTheMaximumConfiguredCrackCount() {
        var settings = new WearSettings(true, new WearSettings.Range(8, 8), java.util.Map.of(), 0, 0, 0);
        int[] source = quad(vertex(0, 0, 0, 0), vertex(1, 0, 1, 0), vertex(1, 1, 1, 1), vertex(0, 1, 0, 1));
        for (long seed = 0; seed < 100; seed++) {
            var pattern = WearPattern.create(seed, settings, "minecraft:golden_pickaxe");
            assertEquals(1, surfaceArea(BrokenIconGeometry.splitVertexData(source, pattern)), 0.00001);
        }
    }

    private static double surfaceArea(List<int[]> pieces) {
        double area = 0;
        for (int[] piece : pieces) {
            double twiceArea = 0;
            for (int i = 0; i < 4; i++) {
                int a = i * STRIDE;
                int b = ((i + 1) % 4) * STRIDE;
                twiceArea += Float.intBitsToFloat(piece[a]) * Float.intBitsToFloat(piece[b + 1])
                    - Float.intBitsToFloat(piece[b]) * Float.intBitsToFloat(piece[a + 1]);
            }
            area += Math.abs(twiceArea) / 2;
        }
        return area;
    }

    private static WearPattern straightCuts(float slope, float... anchors) {
        List<WearPattern.Crack> cracks = new ArrayList<>();
        for (float anchor : anchors) {
            var path = new WearPattern.Path(List.of(new WearPattern.Point(anchor - slope * 0.5F, 0),
                new WearPattern.Point(anchor + slope * 0.5F, 1)));
            cracks.add(new WearPattern.Crack(path, List.of()));
        }
        return new WearPattern(0, cracks, WearSettings.DEFAULT);
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
