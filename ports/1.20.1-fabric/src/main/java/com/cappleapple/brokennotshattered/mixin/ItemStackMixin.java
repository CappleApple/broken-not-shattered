package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.core.BrokenState;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import com.cappleapple.brokennotshattered.core.FunctionalSuppression;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    private ItemStack bns$self() {
        return (ItemStack) (Object) this;
    }

    @Inject(method="hurtAndBreak",at=@At("HEAD"),cancellable=true)
    private <T extends LivingEntity> void bns$stopDamage(int amount,T holder,Consumer<T> onBreak,CallbackInfo callback){
        if(BrokenState.isBroken(bns$self())){if(!holder.level().isClientSide())BreakPatternData.ensureSeed(bns$self());callback.cancel();}
    }
    @Inject(method="hurtAndBreak",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;shrink(I)V"),cancellable=true)
    private <T extends LivingEntity> void bns$preserve(int amount,T holder,Consumer<T> onBreak,CallbackInfo callback){
        if(BrokenState.isHandled(bns$self())){bns$self().setDamageValue(bns$self().getMaxDamage());BreakPatternData.ensureSeed(bns$self());callback.cancel();}
    }
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void bns$backfillBreakSeed(Level level, Entity entity, int slot, boolean selected, CallbackInfo callback) {
        if (!level.isClientSide() && BreakPatternData.ensureSeed(bns$self()) && entity instanceof Player player) {
            player.getInventory().setChanged();
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void bns$useHandMiningSpeed(BlockState state, CallbackInfoReturnable<Float> callback) {
        if (BrokenState.isBroken(bns$self())) {
            callback.setReturnValue(1.0F);
        }
    }

    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void bns$disableToolDrops(BlockState state, CallbackInfoReturnable<Boolean> callback) {
        if (BrokenState.isBroken(bns$self())) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void bns$disableUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> callback) {
        if (BrokenState.isBroken(bns$self())) {
            callback.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void bns$disableUse(
        Level level,
        Player player,
        InteractionHand hand,
        CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callback
    ) {
        if (BrokenState.isBroken(bns$self())) {
            callback.setReturnValue(InteractionResultHolder.pass(bns$self()));
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void bns$disableUseDuration(CallbackInfoReturnable<Integer> callback) {
        if (BrokenState.isBroken(bns$self())) {
            callback.setReturnValue(0);
        }
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void bns$disableRelease(Level level, LivingEntity entity, int timeLeft, CallbackInfo callback) {
        if (BrokenState.isBroken(bns$self())) {
            callback.cancel();
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
    private void bns$disableFinishUsing(
        Level level,
        LivingEntity entity,
        CallbackInfoReturnable<ItemStack> callback
    ) {
        if (BrokenState.isBroken(bns$self())) {
            callback.setReturnValue(bns$self());
        }
    }

    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void bns$disableEntityInteraction(
        Player player,
        LivingEntity target,
        InteractionHand hand,
        CallbackInfoReturnable<InteractionResult> callback
    ) {
        if (BrokenState.isBroken(bns$self())) {
            callback.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method="hurtEnemy",at=@At("HEAD"),cancellable=true)
    private void bns$disableHit(LivingEntity target,Player player,CallbackInfo callback){if(BrokenState.isBroken(bns$self()))callback.cancel();}
    @Inject(method = "mineBlock", at = @At("HEAD"), cancellable = true)
    private void bns$disableToolMining(
        Level level,
        BlockState state,
        BlockPos pos,
        Player player,
        CallbackInfo callback
    ) {
        if (BrokenState.isBroken(bns$self())) {
            callback.cancel();
        }
    }

    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method="getAttributeModifiers",at=@At("RETURN"))
    private com.google.common.collect.Multimap<Attribute,AttributeModifier> bns$filterAttributes(com.google.common.collect.Multimap<Attribute,AttributeModifier> original){return FunctionalSuppression.filter(bns$self(),original);}
}
