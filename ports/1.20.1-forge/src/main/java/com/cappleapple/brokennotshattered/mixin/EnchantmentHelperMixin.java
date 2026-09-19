package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.core.BrokenState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
    @Inject(method = "runIterationOnItem", at = @At("HEAD"), cancellable = true)
    private static void bns$skipBrokenEffects(@org.spongepowered.asm.mixin.injection.Coerce Object visitor, ItemStack stack, CallbackInfo ci) {
        if (BrokenState.isBroken(stack)) ci.cancel();
    }
    @Inject(method = "getDamageBonus", at = @At("HEAD"), cancellable = true)
    private static void bns$skipBrokenDamage(ItemStack stack, net.minecraft.world.entity.MobType type, CallbackInfoReturnable<Float> cir) {
        if (BrokenState.isBroken(stack)) cir.setReturnValue(0.0F);
    }
    @Inject(method = {"getKnockbackBonus", "getFireAspect"}, at = @At("HEAD"), cancellable = true)
    private static void bns$skipBrokenWeaponBonus(net.minecraft.world.entity.LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (BrokenState.isBroken(entity.getMainHandItem())) cir.setReturnValue(0);
    }
}
