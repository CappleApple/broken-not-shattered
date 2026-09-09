package com.cappleapple.brokennotshattered.client;

import static org.junit.jupiter.api.Assertions.*;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import org.junit.jupiter.api.Test;

class WearTextureCoordinatesTest {
    @Test
    void foilKeepsAtlasCoordinatesWhileOnlyTheBaseTextureUsesFullImageCoordinates() {
        RecordingConsumer glint = new RecordingConsumer();
        RecordingConsumer base = new RecordingConsumer();
        VertexConsumer both = VertexMultiConsumer.create(glint,
            new WearTextureCoordinates(base, 0.25F, 0.50F, 0.265625F, 0.53125F));
        both.addVertex(1, 2, 3).setColor(10, 20, 30, 255).setUv(0.2578125F, 0.5234375F)
            .setUv1(4, 5).setUv2(6, 7).setNormal(0, 1, 0);
        assertEquals(0.2578125F, glint.u);
        assertEquals(0.5234375F, glint.v);
        assertEquals(0.5F, base.u);
        assertEquals(0.75F, base.v);
        assertEquals(glint.otherAttributes.toString(), base.otherAttributes.toString(),
            "position, tint, light, overlay and normals must remain unchanged");
    }

    private static final class RecordingConsumer implements VertexConsumer {
        float u, v;
        final StringBuilder otherAttributes = new StringBuilder();
        @Override public VertexConsumer addVertex(float x, float y, float z) { otherAttributes.append(x).append(y).append(z); return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { otherAttributes.append(r).append(g).append(b).append(a); return this; }
        @Override public VertexConsumer setUv(float u, float v) { this.u = u; this.v = v; return this; }
        @Override public VertexConsumer setUv1(int u, int v) { otherAttributes.append(u).append(v); return this; }
        @Override public VertexConsumer setUv2(int u, int v) { otherAttributes.append(u).append(v); return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { otherAttributes.append(x).append(y).append(z); return this; }
    }
}
