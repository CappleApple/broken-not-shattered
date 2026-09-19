package com.cappleapple.brokennotshattered.client;

import static org.junit.jupiter.api.Assertions.*;
import com.cappleapple.brokennotshattered.config.ClientConfig;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class WearPatternTest {




    @Test
    void inclusiveRangesAndItemOverridesAreHonored() {
        WearSettings settings = new WearSettings(true, new WearSettings.Range(4, 2),
            Map.of("minecraft:golden_pickaxe", new WearSettings.Range(6, 6)), 0, 0, 0);
        Set<Integer> counts = new HashSet<>();
        for (long seed = 0; seed < 1000; seed++) {
            int count = WearPattern.create(seed, settings, "minecraft:iron_pickaxe").cracks().size();
            assertTrue(count >= 2 && count <= 4);
            counts.add(count);
            assertEquals(6, WearPattern.create(seed, settings, "minecraft:golden_pickaxe").cracks().size());
        }
        assertEquals(Set.of(2, 3, 4), counts);
        assertTrue(ClientConfig.validBreakLineOverride("minecraft:golden_pickaxe=2-5"));
        assertFalse(ClientConfig.validBreakLineOverride("minecraft:golden_pickaxe=0-99"));
        assertFalse(ClientConfig.validBreakLineOverride("bad id=1-2"));
    }

    @Test
    void disabledModifiersAndZeroCracksPreservePixelsExactly() {
        WearPattern pattern = WearPattern.create(5,
            new WearSettings(true, new WearSettings.Range(0, 0), Map.of(), 0, 0, 0), "minecraft:golden_pickaxe");
        for (int value : List.of(0xFF00AAEE, 0x00010203, 0x8055AAFF, 0xFFFFFFFF)) {
            assertEquals(value, pattern.modify(value, 2, 3, 16, 16));
        }
    }

    @Test
    void savedSeedsReproduceBoundedPathsWithInteriorStopsAndRepeatedForks() {
        WearSettings settings = new WearSettings(true, new WearSettings.Range(8, 8), Map.of(), 0, 0, 0);
        int partial = 0;
        int complete = 0;
        int repeatedForks = 0;
        int jagged = 0;
        for (long seed = 0; seed < 100; seed++) {
            WearPattern pattern = WearPattern.create(seed, settings, "minecraft:golden_pickaxe");
            assertEquals(pattern, WearPattern.create(seed, settings, "minecraft:golden_pickaxe"));
            for (WearPattern.Crack crack : pattern.cracks()) {
                if (crack.trunk().through()) complete++;
                else partial++;
                if (crack.branches().size() >= 2) repeatedForks++;
                var trunk = crack.trunk().points();
                assertTrue(trunk.size() >= 6 && trunk.size() <= 13);
                int turns = 0;
                for (int i = 2; i < trunk.size(); i++) {
                    float previous = trunk.get(i - 1).x() - trunk.get(i - 2).x();
                    float current = trunk.get(i).x() - trunk.get(i - 1).x();
                    if (previous * current < 0) turns++;
                    assertTrue(trunk.get(i).y() > trunk.get(i - 1).y());
                }
                if (turns >= 2) jagged++;
                assertTrue(crack.branches().size() <= 4, "forking must have a fixed complexity bound");
                Set<WearPattern.Point> connected = new HashSet<>(trunk);
                for (WearPattern.Path branch : crack.branches()) {
                    assertTrue(connected.contains(branch.points().getFirst()), "each fork must join an existing path");
                    assertTrue(branch.points().size() >= 4 && branch.points().size() <= 6);
                    for (WearPattern.Point point : branch.points()) {
                        assertTrue(point.x() > 0 && point.x() < 1);
                        assertTrue(point.y() > 0 && point.y() < 1, "forks must stop inside the texture");
                    }
                    connected.addAll(branch.points());
                }
            }
            // Multiple complete seams must remain ordered at every bend.
            var seams = pattern.seams();
            for (int i = 1; i < seams.size(); i++) for (int y = 0; y <= 120; y++) {
                assertTrue(seams.get(i).xAt(y / 120F) > seams.get(i - 1).xAt(y / 120F));
            }
        }
        assertTrue(partial > complete * 2, "most cracks should stop instead of severing the entire item");
        assertTrue(complete > 0, "complete fractures should still occur occasionally");
        assertTrue(repeatedForks > 100, "multiple forks should occur across the generated items");
        assertTrue(jagged > 600, "cracks should commonly turn back and forth more than once");
    }

    @Test
    void textureCracksStopAtFiniteEndpointsAndDrawTheirBranches() {
        var trunk = new WearPattern.Path(List.of(new WearPattern.Point(0.5F, 0.3F),
            new WearPattern.Point(0.62F, 0.45F), new WearPattern.Point(0.48F, 0.6F)));
        var branch = new WearPattern.Path(List.of(trunk.points().get(1),
            new WearPattern.Point(0.7F, 0.51F), new WearPattern.Point(0.72F, 0.57F)));
        WearPattern pattern = new WearPattern(10, List.of(new WearPattern.Crack(trunk, List.of(branch))),
            new WearSettings(true, new WearSettings.Range(1, 1), Map.of(), 0, 0, 0));
        assertEquals(0xFFFFFFFF, pattern.modify(0xFFFFFFFF, 32, 60, 64, 64), "no extension below the start");
        assertEquals(0xFFFFFFFF, pattern.modify(0xFFFFFFFF, 30, 6, 64, 64), "no extension beyond the end");
        assertTrue((pattern.modify(0xFFFFFFFF, 32, 44, 64, 64) & 255) < 100, "trunk must be visible");
        assertTrue((pattern.modify(0xFFFFFFFF, 44, 31, 64, 64) & 255) < 100, "branch must be visible");
        assertTrue(pattern.seams().isEmpty(), "partial paths must not create a full-model split");
    }

    @Test
    void wearPreservesAlphaDoesNotEditInputAndRepeatsAcrossAnimationFrames() {
        WearPattern pattern = WearPattern.create(21, WearSettings.DEFAULT, "minecraft:golden_pickaxe");
        try (NativeImage source = new NativeImage(16, 32, true)) {
            for (int y = 0; y < 32; y++) for (int x = 0; x < 16; x++) {
                source.setPixelABGR(x, y, x == 0 ? 0 : 0x80AAEEFF);
            }
            try (NativeImage result = WornTextures.modify(source, pattern, 16, 16)) {
                int changed = 0;
                for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                    int original = net.minecraft.util.ARGB.toABGR(source.getPixel(x, y));
                    int output = net.minecraft.util.ARGB.toABGR(result.getPixel(x, y));
                    assertEquals(x == 0 ? 0 : 0x80AAEEFF, original);
                    assertEquals(original >>> 24, output >>> 24);
                    assertEquals(output, net.minecraft.util.ARGB.toABGR(result.getPixel(x, y + 16)));
                    if (output != original) changed++;
                }
                assertTrue(changed > 200, "wear should visibly modify the opaque surface");
            }
        }
    }
}
