package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.core.BrokenState;
import com.cappleapple.brokennotshattered.core.BreakPatternData;
import java.util.function.Consumer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
abstract class ItemStackMixin {
    private ItemStack bns$self() {
        return (ItemStack) (Object) this;
    }

    @Inject(
        method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void bns$stopFurtherDamage(
        int amount,
        LivingEntity holder,
        Consumer<LivingEntity> onBreak,
        CallbackInfo callback
    ) {
        if (BrokenState.isBroken(bns$self())) {
            if (!holder.level().isClientSide()) BreakPatternData.ensureSeed(bns$self());
            callback.cancel();
        }
    }

    @Inject(
        method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
        cancellable = true
    )
    private void bns$preserveAtZeroDurability(
        int amount,
        LivingEntity holder,
        Consumer<LivingEntity> onBreak,
        CallbackInfo callback
    ) {
        ItemStack stack = bns$self();
        if (BrokenState.isHandled(stack)) {
            stack.setDamageValue(stack.getMaxDamage());
            BreakPatternData.ensureSeed(stack);
            callback.cancel();
        }
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

    /** Concrete override of NeoForge's ItemStack extension default, covering vanilla and modded ToolAction implementations. */
    public boolean canPerformAction(ToolAction ability) {
        ItemStack stack = bns$self();
        return !BrokenState.isBroken(stack) && stack.getItem().canPerformAction(stack, ability);
    }

    /** Concrete override of NeoForge's ItemStack extension default so custom elytra items obey brokenness too. */
    public boolean canElytraFly(LivingEntity entity) {
        ItemStack stack = bns$self();
        return !BrokenState.isBroken(stack) && stack.getItem().canElytraFly(stack, entity);
    }

    public boolean elytraFlightTick(LivingEntity entity, int flightTicks) {
        ItemStack stack = bns$self();
        return !BrokenState.isBroken(stack) && stack.getItem().elytraFlightTick(stack, entity, flightTicks);
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void bns$disableUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> callback) {
        if (BrokenState.isBroken(bns$self())) {
            callback.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "onItemUseFirst", at = @At("HEAD"), cancellable = true, remap = false)
    private void bns$disableUseFirst(UseOnContext context, CallbackInfoReturnable<InteractionResult> callback) {
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

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    private void bns$disableWeaponHit(
        LivingEntity target,
        Player player,
        CallbackInfo callback
    ) {
        if (BrokenState.isBroken(bns$self())) {
            callback.cancel();
        }
    }

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

}
