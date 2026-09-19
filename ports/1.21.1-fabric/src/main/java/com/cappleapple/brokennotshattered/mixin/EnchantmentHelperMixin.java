package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.core.BrokenState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
    @ModifyArg(
        method = {"isImmuneToDamage", "getDamageProtection", "doPostAttackEffectsWithItemSource"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;runIterationOnEquipment(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor;)V"
        ),
        index = 1
    )
    private static EnchantmentHelper.EnchantmentInSlotVisitor bns$skipBrokenDefensiveEnchantments(
        EnchantmentHelper.EnchantmentInSlotVisitor original
    ) {
        return (enchantment, level, inUse) -> {
            if (!BrokenState.isBroken(inUse.itemStack())) {
                original.accept(enchantment, level, inUse);
            }
        };
    }

    @Inject(method = "modifyDamage", at = @At("HEAD"), cancellable = true)
    private static void bns$disableWeaponDamageEnchantments(
        ServerLevel level,
        ItemStack stack,
        Entity target,
        DamageSource source,
        float damage,
        CallbackInfoReturnable<Float> callback
    ) {
        if (BrokenState.isBroken(stack)) {
            callback.setReturnValue(damage);
        }
    }

    @Inject(method = "modifyFallBasedDamage", at = @At("HEAD"), cancellable = true)
    private static void bns$disableFallWeaponEnchantments(
        ServerLevel level,
        ItemStack stack,
        Entity target,
        DamageSource source,
        float damage,
        CallbackInfoReturnable<Float> callback
    ) {
        if (BrokenState.isBroken(stack)) {
            callback.setReturnValue(damage);
        }
    }

    @Inject(method = "modifyArmorEffectiveness", at = @At("HEAD"), cancellable = true)
    private static void bns$disableArmorEffectivenessEnchantments(
        ServerLevel level,
        ItemStack stack,
        Entity target,
        DamageSource source,
        float effectiveness,
        CallbackInfoReturnable<Float> callback
    ) {
        if (BrokenState.isBroken(stack)) {
            callback.setReturnValue(effectiveness);
        }
    }

    @Inject(method = "modifyKnockback", at = @At("HEAD"), cancellable = true)
    private static void bns$disableWeaponKnockbackEnchantments(
        ServerLevel level,
        ItemStack stack,
        Entity target,
        DamageSource source,
        float knockback,
        CallbackInfoReturnable<Float> callback
    ) {
        if (BrokenState.isBroken(stack)) {
            callback.setReturnValue(knockback);
        }
    }

    @ModifyVariable(method = "doPostAttackEffectsWithItemSource", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static ItemStack bns$disableWeaponPostAttackEnchantments(ItemStack sourceStack) {
        return sourceStack != null && BrokenState.isBroken(sourceStack) ? null : sourceStack;
    }
}
