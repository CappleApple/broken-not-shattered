package com.cappleapple.brokennotshattered.core;

import java.util.Set;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.ItemAttributeModifierEvent;

/** Suppresses combat attributes without changing stored item data or attack speed. */
public final class FunctionalSuppression {
    private static final Set<Attribute> DISABLED_COMBAT_ATTRIBUTES = Set.of(
        Attributes.ATTACK_DAMAGE, Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.KNOCKBACK_RESISTANCE);
    private FunctionalSuppression() {}
    public static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        if (BrokenState.isBroken(event.getItemStack())) {
            DISABLED_COMBAT_ATTRIBUTES.forEach(event::removeAttribute);
        }
    }
}
