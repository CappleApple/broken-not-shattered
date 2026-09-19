package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.core.BrokenState;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.cappleapple.brokennotshattered.core.FunctionalSuppression;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.apache.commons.lang3.function.TriConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    private ItemStack bns$self() { return (ItemStack) (Object) this; }

    @Inject(method = "processDurabilityChange", at = @At("HEAD"), cancellable = true)
    private void bns$stopFurtherDamage(CallbackInfoReturnable<Integer> callback) {
        if (BrokenState.isBroken(bns$self())) {
            BreakPatternData.ensureSeed(bns$self());
            callback.setReturnValue(0);
        }
    }
    @Inject(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), cancellable = true)
    private void bns$preserve(int amount, ServerPlayer player, Consumer<ItemStack> onBreak, CallbackInfo callback) {
        ItemStack stack = bns$self();
        if (BrokenState.isHandled(stack)) {
            stack.setDamageValue(stack.getMaxDamage());
            BreakPatternData.ensureSeed(stack);
            onBreak.accept(stack.copy());
            callback.cancel();
        }
    }
    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void bns$handSpeed(CallbackInfoReturnable<Float> callback) {
        if (BrokenState.isBroken(bns$self())) callback.setReturnValue(1.0F);
    }
    @Inject(method = {"isCorrectToolForDrops", "hurtEnemy"}, at = @At("HEAD"), cancellable = true)
    private void bns$disableTool(CallbackInfoReturnable<Boolean> callback) {
        if (BrokenState.isBroken(bns$self())) callback.setReturnValue(false);
    }
    @Inject(method = {"useOn", "use", "interactLivingEntity"}, at = @At("HEAD"), cancellable = true)
    private void bns$disableUse(CallbackInfoReturnable<InteractionResult> callback) {
        if (BrokenState.isBroken(bns$self())) callback.setReturnValue(InteractionResult.PASS);
    }
    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void bns$disableUseDuration(CallbackInfoReturnable<Integer> callback) {
        if (BrokenState.isBroken(bns$self())) callback.setReturnValue(0);
    }
    @Inject(method = {"releaseUsing", "postHurtEnemy", "mineBlock", "onUseTick"}, at = @At("HEAD"), cancellable = true)
    private void bns$disableAction(CallbackInfo callback) {
        if (BrokenState.isBroken(bns$self())) callback.cancel();
    }
    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void bns$disableFinish(CallbackInfoReturnable<ItemStack> callback) {
        if (BrokenState.isBroken(bns$self())) callback.setReturnValue(bns$self());
    }
    @ModifyArg(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"), index = 1)
    private BiConsumer<Holder<Attribute>, AttributeModifier> bns$filterSlot(BiConsumer<Holder<Attribute>, AttributeModifier> original) {
        if (!BrokenState.isBroken(bns$self())) return original;
        return (attribute, modifier) -> { if (!FunctionalSuppression.DISABLED_COMBAT_ATTRIBUTES.contains(attribute)) original.accept(attribute, modifier); };
    }
    @ModifyArg(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ItemAttributeModifiers;forEach(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V"), index = 1)
    private TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> bns$filterGroup(TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> original) {
        if (!BrokenState.isBroken(bns$self())) return original;
        return (attribute, modifier, display) -> { if (!FunctionalSuppression.DISABLED_COMBAT_ATTRIBUTES.contains(attribute)) original.accept(attribute, modifier, display); };
    }
    @ModifyArg(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"), index = 2)
    private BiConsumer<Holder<Attribute>, AttributeModifier> bns$filterEnchantmentSlot(BiConsumer<Holder<Attribute>, AttributeModifier> original) {
        return FunctionalSuppression.filterEnchantmentAttributes(bns$self(), original);
    }
    @ModifyArg(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;forEachModifier(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V"), index = 2)
    private BiConsumer<Holder<Attribute>, AttributeModifier> bns$filterEnchantmentGroup(BiConsumer<Holder<Attribute>, AttributeModifier> original) {
        return FunctionalSuppression.filterEnchantmentAttributes(bns$self(), original);
    }
}
