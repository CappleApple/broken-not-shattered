package com.cappleapple.brokennotshattered.core;

import com.cappleapple.brokennotshattered.registry.ModItemTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * The single authoritative interpretation of whether a stack is covered and broken.
 * No state is stored: every result is derived from the stack's current components and tags.
 */
public final class BrokenState {
    public enum Disposition {
        IGNORED,
        SHATTERS,
        HANDLED,
        OUTSIDE
    }

    private BrokenState() {
    }

    public static boolean isIgnored(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModItemTags.IGNORE);
    }

    public static boolean shattersNormally(ItemStack stack) {
        return !isIgnored(stack) && !stack.isEmpty() && stack.is(ModItemTags.SHATTERS);
    }

    public static Disposition disposition(ItemStack stack) {
        if (stack.isEmpty()) {
            return Disposition.OUTSIDE;
        }

        boolean protectedDurability = stack.is(ModItemTags.PROTECTED)
            && stack.has(DataComponents.DAMAGE)
            && stack.has(DataComponents.MAX_DAMAGE)
            && safeMaxDamage(stack) > 0;
        return disposition(isIgnored(stack), stack.is(ModItemTags.SHATTERS), stack.isDamageableItem(), protectedDurability);
    }

    public static Disposition disposition(
        boolean ignored,
        boolean shatters,
        boolean normallyDamageable,
        boolean protectedDurability
    ) {
        if (ignored) {
            return Disposition.IGNORED;
        }
        if (shatters) {
            return Disposition.SHATTERS;
        }
        if (normallyDamageable || protectedDurability) {
            return Disposition.HANDLED;
        }
        return Disposition.OUTSIDE;
    }

    public static boolean isHandled(ItemStack stack) {
        return disposition(stack) == Disposition.HANDLED;
    }

    public static boolean isBroken(ItemStack stack) {
        if (!isHandled(stack)) {
            return false;
        }

        int maxDamage = safeMaxDamage(stack);
        return maxDamage > 0 && stack.getDamageValue() >= maxDamage;
    }

    private static int safeMaxDamage(ItemStack stack) {
        return Math.max(0, stack.getMaxDamage());
    }
}
