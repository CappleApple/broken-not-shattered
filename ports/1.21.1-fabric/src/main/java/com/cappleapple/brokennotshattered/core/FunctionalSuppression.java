package com.cappleapple.brokennotshattered.core;

import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

/**
 * Dynamically suppresses standard combat modifiers without mutating the stack's component data.
 * The allowlist is deliberately limited to normal weapon and armor combat attributes so unrelated
 * modded utility attributes remain compatible.
 */
public final class FunctionalSuppression {
    private static final Set<Holder<Attribute>> DISABLED_COMBAT_ATTRIBUTES = Set.of(
        Attributes.ATTACK_DAMAGE,
        Attributes.ARMOR,
        Attributes.ARMOR_TOUGHNESS,
        Attributes.KNOCKBACK_RESISTANCE
    );

    private FunctionalSuppression() {
    }

    public static BiConsumer<Holder<Attribute>, AttributeModifier> filterAttributes(ItemStack stack,
        BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        return !BrokenState.isBroken(stack) ? consumer : (attribute, modifier) -> {
            if (!DISABLED_COMBAT_ATTRIBUTES.contains(attribute)) consumer.accept(attribute, modifier);
        };
    }
    /**
     * Broken items retain attack-speed enchantment attributes while other enchantment attributes remain inactive.
     * The ordinary component/default attribute path is filtered by the item modifier mixin.
     */
    public static BiConsumer<Holder<Attribute>, AttributeModifier> filterEnchantmentAttributes(
        ItemStack stack,
        BiConsumer<Holder<Attribute>, AttributeModifier> consumer
    ) {
        return filterEnchantmentAttributes(BrokenState.isBroken(stack), consumer);
    }

    static BiConsumer<Holder<Attribute>, AttributeModifier> filterEnchantmentAttributes(
        boolean broken,
        BiConsumer<Holder<Attribute>, AttributeModifier> consumer
    ) {
        if (!broken) {
            return consumer;
        }

        return (attribute, modifier) -> {
            if (Attributes.ATTACK_SPEED.equals(attribute)) {
                consumer.accept(attribute, modifier);
            }
        };
    }
}
