package com.cappleapple.brokennotshattered.core;

import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;

/**
 * Dynamically suppresses standard combat modifiers without mutating the stack's component data.
 * The allowlist is deliberately limited to normal weapon and armor combat attributes so unrelated
 * modded utility attributes remain compatible.
 */
public final class FunctionalSuppression {
    private static final Set<Holder<Attribute>> DISABLED_COMBAT_ATTRIBUTES = Set.of(
        Attributes.ATTACK_DAMAGE,
        Attributes.ATTACK_SPEED,
        Attributes.ARMOR,
        Attributes.ARMOR_TOUGHNESS,
        Attributes.KNOCKBACK_RESISTANCE
    );

    private FunctionalSuppression() {
    }

    public static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        if (BrokenState.isBroken(event.getItemStack())) {
            event.removeIf(entry -> DISABLED_COMBAT_ATTRIBUTES.contains(entry.attribute()));
        }
    }

    public static void onSweepAttack(SweepAttackEvent event) {
        if (BrokenState.isBroken(event.getEntity().getMainHandItem())) {
            event.setSweeping(false);
        }
    }
}
