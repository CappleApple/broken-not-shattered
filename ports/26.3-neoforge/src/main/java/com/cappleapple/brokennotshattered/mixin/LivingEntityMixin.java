package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.core.BrokenState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    @Inject(method = "canGlideUsing", at = @At("HEAD"), cancellable = true)
    private static void bns$disableGlider(ItemStack stack, EquipmentSlot slot, CallbackInfoReturnable<Boolean> callback) {
        if (BrokenState.isBroken(stack)) callback.setReturnValue(false);
    }
}
