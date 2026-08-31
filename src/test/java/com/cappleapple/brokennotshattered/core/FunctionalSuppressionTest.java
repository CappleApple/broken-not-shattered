package com.cappleapple.brokennotshattered.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

class FunctionalSuppressionTest {
    private static final AttributeModifier TEST_MODIFIER = new AttributeModifier(
        ResourceLocation.fromNamespaceAndPath("broken_not_shattered", "test"),
        0.25,
        AttributeModifier.Operation.ADD_VALUE
    );

    @Test
    void brokenEnchantmentFilterRetainsOnlyAttackSpeed() {
        List<Holder<Attribute>> retained = new ArrayList<>();
        var filter = FunctionalSuppression.filterEnchantmentAttributes(true, (attribute, modifier) -> retained.add(attribute));

        filter.accept(Attributes.ATTACK_DAMAGE, TEST_MODIFIER);
        filter.accept(Attributes.ARMOR, TEST_MODIFIER);
        filter.accept(Attributes.ATTACK_SPEED, TEST_MODIFIER);

        assertEquals(List.of(Attributes.ATTACK_SPEED), retained);
    }

    @Test
    void repairedEnchantmentFilterPassesEverythingThrough() {
        List<Holder<Attribute>> retained = new ArrayList<>();
        var filter = FunctionalSuppression.filterEnchantmentAttributes(false, (attribute, modifier) -> retained.add(attribute));

        filter.accept(Attributes.ATTACK_DAMAGE, TEST_MODIFIER);
        filter.accept(Attributes.ATTACK_SPEED, TEST_MODIFIER);

        assertEquals(List.of(Attributes.ATTACK_DAMAGE, Attributes.ATTACK_SPEED), retained);
    }
}
