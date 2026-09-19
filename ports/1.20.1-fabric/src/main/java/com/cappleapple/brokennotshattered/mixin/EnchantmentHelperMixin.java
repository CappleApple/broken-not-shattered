package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.core.BrokenState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(EnchantmentHelper.class)
abstract class EnchantmentHelperMixin {
    @ModifyVariable(method="runIterationOnItem",at=@At("HEAD"),argsOnly=true)
    private static ItemStack bns$skipBrokenEffects(ItemStack stack){return BrokenState.isBroken(stack)?ItemStack.EMPTY:stack;}
    @Inject(method="getDamageBonus",at=@At("HEAD"),cancellable=true)
    private static void bns$damage(ItemStack stack,MobType target,CallbackInfoReturnable<Float> ci){if(BrokenState.isBroken(stack))ci.setReturnValue(0f);}
    @Inject(method={"getKnockbackBonus","getFireAspect"},at=@At("HEAD"),cancellable=true)
    private static void bns$weaponEffects(LivingEntity entity,CallbackInfoReturnable<Integer> ci){if(BrokenState.isBroken(entity.getMainHandItem()))ci.setReturnValue(0);}
}
