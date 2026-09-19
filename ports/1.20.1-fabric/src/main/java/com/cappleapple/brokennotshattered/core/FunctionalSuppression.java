package com.cappleapple.brokennotshattered.core;
import java.util.Set;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
public final class FunctionalSuppression {
    private static final Set<Attribute> DISABLED=Set.of(Attributes.ATTACK_DAMAGE,Attributes.ARMOR,Attributes.ARMOR_TOUGHNESS,Attributes.KNOCKBACK_RESISTANCE);
    public static Multimap<Attribute,AttributeModifier> filter(ItemStack stack,Multimap<Attribute,AttributeModifier> original){
        if(!BrokenState.isBroken(stack))return original;
        var result=ImmutableMultimap.<Attribute,AttributeModifier>builder();
        original.forEach((attribute,modifier)->{if(!DISABLED.contains(attribute))result.put(attribute,modifier);});return result.build();
    }
}
