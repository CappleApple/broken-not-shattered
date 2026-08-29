package com.cappleapple.brokennotshattered.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class BrokenStateTest {
    @Test
    void derivesBrokennessOnlyFromCurrentDurability() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.setDamageValue(stack.getMaxDamage() - 1);

        assertFalse(BrokenState.isBroken(stack));

        stack.setDamageValue(stack.getMaxDamage());
        assertTrue(BrokenState.isBroken(stack));

        stack.setDamageValue(stack.getMaxDamage() - 1);
        assertFalse(BrokenState.isBroken(stack));
    }

    @Test
    void ignoresNonDamageableAndInvalidStacks() {
        assertFalse(BrokenState.isBroken(ItemStack.EMPTY));
        assertFalse(BrokenState.isBroken(new ItemStack(Items.STONE)));
    }

    @Test
    void tagPriorityIsIgnoreThenShatterThenHandling() {
        assertEquals(
            BrokenState.Disposition.IGNORED,
            BrokenState.disposition(true, true, true, true)
        );
        assertEquals(
            BrokenState.Disposition.SHATTERS,
            BrokenState.disposition(false, true, true, true)
        );
        assertEquals(
            BrokenState.Disposition.HANDLED,
            BrokenState.disposition(false, false, true, false)
        );
        assertEquals(
            BrokenState.Disposition.HANDLED,
            BrokenState.disposition(false, false, false, true)
        );
        assertEquals(
            BrokenState.Disposition.OUTSIDE,
            BrokenState.disposition(false, false, false, false)
        );
    }
}
