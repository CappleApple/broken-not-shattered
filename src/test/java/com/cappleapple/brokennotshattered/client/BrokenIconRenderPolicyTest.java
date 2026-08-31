package com.cappleapple.brokennotshattered.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.world.item.ItemDisplayContext;
import org.junit.jupiter.api.Test;

class BrokenIconRenderPolicyTest {
    private static final List<ItemDisplayContext> HELD_AND_DROPPED = List.of(
        ItemDisplayContext.GROUND,
        ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
        ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
        ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
        ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
    );

    @Test
    void guiIconsAlwaysUseTheBrokenSplit() {
        assertTrue(BrokenIconRenderPolicy.shouldSplit(ItemDisplayContext.GUI, false));
        assertTrue(BrokenIconRenderPolicy.shouldSplit(ItemDisplayContext.GUI, true));
    }

    @Test
    void heldAndDroppedModelsSplitOnlyWhenFlat() {
        for (ItemDisplayContext displayContext : HELD_AND_DROPPED) {
            assertTrue(
                BrokenIconRenderPolicy.shouldSplit(displayContext, false),
                displayContext + " did not split a flat model"
            );
            assertFalse(
                BrokenIconRenderPolicy.shouldSplit(displayContext, true),
                displayContext + " split a 3D model"
            );
        }
    }

    @Test
    void unrelatedFlatContextsRemainUntouched() {
        assertFalse(BrokenIconRenderPolicy.shouldSplit(ItemDisplayContext.FIXED, false));
        assertFalse(BrokenIconRenderPolicy.shouldSplit(ItemDisplayContext.HEAD, false));
        assertFalse(BrokenIconRenderPolicy.shouldSplit(ItemDisplayContext.NONE, false));
    }
}
