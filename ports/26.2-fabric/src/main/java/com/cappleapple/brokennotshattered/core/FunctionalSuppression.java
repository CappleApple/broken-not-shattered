package com.cappleapple.brokennotshattered.core;

import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class FunctionalSuppression {
    public static final Set<Holder<Attribute>> DISABLED_COMBAT_ATTRIBUTES = Set.of(
        Attributes.ATTACK_DAMAGE, Attributes.ARMOR, Attributes.ARMOR_TOUGHNESS, Attributes.KNOCKBACK_RESISTANCE);
    private FunctionalSuppression() {}
    public static BiConsumer<Holder<Attribute>, AttributeModifier> filterEnchantmentAttributes(ItemStack stack,
            BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        return filterEnchantmentAttributes(BrokenState.isBroken(stack), consumer);
    }
    static BiConsumer<Holder<Attribute>, AttributeModifier> filterEnchantmentAttributes(boolean broken,
            BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
        if (!broken) return consumer;
        return (attribute, modifier) -> { if (Attributes.ATTACK_SPEED.equals(attribute)) consumer.accept(attribute, modifier); };
    }
}
