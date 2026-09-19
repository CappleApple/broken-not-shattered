package com.cappleapple.brokennotshattered.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class ModernItemWearTest {
    @Test
    void geometryRoundTripRetainsAsymmetricUvsPositionsColorsAndNormals() {
        long uv = UVPair.pack(.125f, .75f);
        BakedColors colors = BakedColors.of(0xFF204060, 0xFF60A0E0, 0xFFC08040, 0xFF408020);
        BakedNormals normals = BakedNormals.of(BakedNormals.pack(0, 0, 1), BakedNormals.pack(1, 0, 0),
            BakedNormals.pack(0, 1, 0), BakedNormals.pack(-1, 0, 0));
        var source = new BakedQuad(new Vector3f(0, 0, .45f), new Vector3f(1, 0, .45f),
            new Vector3f(1, 1, .45f), new Vector3f(0, 1, .45f), uv, uv, uv, uv,
            Direction.SOUTH, null, normals, colors);
        int[] vertices = ModernItemWear.vertices(source);
        var copy = ModernItemWear.quad(vertices, Direction.SOUTH, source);
        assertEquals(.125f, UVPair.unpackU(copy.packedUV0()));
        assertEquals(.75f, UVPair.unpackV(copy.packedUV0()));
        assertEquals(source.position2(), copy.position2());
        for (int vertex = 0; vertex < 4; vertex++) {
            assertEquals(colors.color(vertex), vertices[vertex * 8 + 3]);
            assertEquals(normals.normal(vertex), vertices[vertex * 8 + 7]);
            assertEquals(colors.color(vertex), copy.bakedColors().color(vertex));
            assertEquals(normals.normal(vertex), copy.bakedNormals().normal(vertex));
        }
    }

    @Test
    void splitQuadsUseInterpolatedColorsAtTheirNewVertices() {
        var source = coloredFront();
        var pieces = BrokenIconGeometry.splitAll(List.of(source), false, seam(.5f, .5f));
        assertEquals(2, pieces.size());
        int cutVertices = 0;
        for (BakedQuad piece : pieces) {
            for (int vertex = 0; vertex < 4; vertex++) {
                if (UVPair.unpackU(piece.packedUV(vertex)) == .5f) {
                    int expected = piece.position(vertex).y() == 0 ? 0xFF4070A0 : 0xFF808030;
                    assertEquals(expected, piece.bakedColors().color(vertex),
                        "a clipped vertex must interpolate the original edge colors");
                    cutVertices++;
                }
            }
        }
        assertEquals(4, cutVertices);
    }

    @Test
    void generatedCapsRetainTheirSlopedNormalsAndOwnColor() {
        var source = coloredFront();
        int capColor = 0xFF123456;
        var caps = BrokenIconGeometry.createCapVertexData(16, 16, .45f, .55f,
            capColor, 0, 0, 1, 0, 1, seam(.25f, .75f));
        assertFalse(caps.isEmpty());
        for (var cap : caps) {
            var quad = ModernItemWear.quad(cap.vertices(), cap.direction(), source);
            for (int vertex = 0; vertex < 4; vertex++) {
                int expectedNormal = cap.vertices()[vertex * 8 + 7];
                assertNotEquals(0, BakedNormals.unpackY(expectedNormal),
                    "the sloped cut must produce a non-cardinal normal");
                assertNotEquals(source.bakedNormals().normal(vertex), expectedNormal);
                assertEquals(expectedNormal, quad.bakedNormals().normal(vertex));
                assertEquals(capColor, quad.bakedColors().color(vertex));
            }
        }
    }

    private static BakedQuad coloredFront() {
        return new BakedQuad(new Vector3f(0, 0, .45f), new Vector3f(1, 0, .45f),
            new Vector3f(1, 1, .45f), new Vector3f(0, 1, .45f),
            UVPair.pack(0, 0), UVPair.pack(1, 0), UVPair.pack(1, 1), UVPair.pack(0, 1),
            Direction.SOUTH, null, BakedNormals.of(BakedNormals.pack(0, 0, 1)),
            BakedColors.of(0xFF204060, 0xFF60A0E0, 0xFFC08040, 0xFF408020));
    }

    private static WearPattern seam(float bottom, float top) {
        var path = new WearPattern.Path(List.of(new WearPattern.Point(bottom, 0), new WearPattern.Point(top, 1)));
        return new WearPattern(1L, List.of(new WearPattern.Crack(path, List.of())), WearSettings.DEFAULT);
    }
}
