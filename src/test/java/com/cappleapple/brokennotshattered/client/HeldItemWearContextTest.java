package com.cappleapple.brokennotshattered.client;

import static org.junit.jupiter.api.Assertions.*;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class HeldItemWearContextTest {
    @Test
    void temporaryCopiesKeepTheEquippedPatternAndSeparateHandsStayUnique() {
        ItemStack main = brokenPick();
        ItemStack off = brokenPick();
        WearPattern expected = BrokenAppearance.pattern(main);
        for (int frame = 0; frame < 100; frame++) {
            ItemStack copy = main.copy();
            ItemStack owner = HeldItemWearContext.select(copy, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                HumanoidArm.RIGHT, main, off);
            assertSame(main, owner);
            assertSame(expected, BrokenAppearance.pattern(owner));
            assertSame(off, HeldItemWearContext.select(off.copy(), ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                HumanoidArm.RIGHT, main, off));
        }
        assertNotEquals(expected.seed(), BrokenAppearance.pattern(off).seed());
        assertEquals(expected.seed(), BrokenAppearance.pattern(main.copy()).seed(),
            "copies must retain their saved appearance");
    }

    @Test
    void leftHandedPlayersMapBothFirstAndThirdPersonToTheCorrectSlot() {
        ItemStack main = brokenPick();
        ItemStack off = main.copy();
        for (ItemDisplayContext context : new ItemDisplayContext[] {
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND}) {
            assertSame(main, HeldItemWearContext.select(main.copy(), context, HumanoidArm.LEFT, main, off));
        }
        for (ItemDisplayContext context : new ItemDisplayContext[] {
            ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND}) {
            assertSame(off, HeldItemWearContext.select(off.copy(), context, HumanoidArm.LEFT, main, off));
        }
    }

    @Test
    void unrelatedContextsAndCosmeticOrOldEquipStacksRetainTheirOwnIdentity() {
        ItemStack equipped = brokenPick();
        ItemStack copy = equipped.copy();
        for (ItemDisplayContext context : new ItemDisplayContext[] {
            ItemDisplayContext.GUI, ItemDisplayContext.GROUND, ItemDisplayContext.FIXED, ItemDisplayContext.HEAD}) {
            assertSame(copy, HeldItemWearContext.select(copy, context, HumanoidArm.RIGHT, equipped, ItemStack.EMPTY));
        }
        copy.set(DataComponents.CUSTOM_NAME, Component.literal("Cosmetic"));
        assertSame(copy, HeldItemWearContext.select(copy, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
            HumanoidArm.RIGHT, equipped, ItemStack.EMPTY));
        ItemStack old = equipped.copy();
        equipped.setDamageValue(0);
        assertSame(old, HeldItemWearContext.select(old, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
            HumanoidArm.RIGHT, equipped, ItemStack.EMPTY));
        assertNull(BrokenAppearance.pattern(equipped), "repaired equipment must immediately lose its wear");
    }

    @Test
    void nestedScopesRestoreTheirParentEvenWhenRenderingThrows() {
        ItemStack outer = brokenPick();
        ItemStack inner = brokenPick();
        ItemStack outerCopy = outer.copy();
        ItemStack innerCopy = inner.copy();
        try (var parent = HeldItemWearContext.enter(outerCopy, outer)) {
            assertSame(outer, HeldItemWearContext.identity(outerCopy));
            assertThrows(IllegalStateException.class, () -> {
                try (var child = HeldItemWearContext.enter(innerCopy, inner)) {
                    assertSame(inner, HeldItemWearContext.identity(innerCopy));
                    assertSame(outerCopy, HeldItemWearContext.identity(outerCopy));
                    throw new IllegalStateException("renderer failed");
                }
            });
            assertSame(outer, HeldItemWearContext.identity(outerCopy));
            assertSame(innerCopy, HeldItemWearContext.identity(innerCopy));
        }
        assertSame(outerCopy, HeldItemWearContext.identity(outerCopy));
    }

    private static ItemStack brokenPick() {
        ItemStack stack = new ItemStack(Items.GOLDEN_PICKAXE);
        stack.setDamageValue(stack.getMaxDamage());
        BreakPatternData.ensureSeed(stack);
        return stack;
    }
}
