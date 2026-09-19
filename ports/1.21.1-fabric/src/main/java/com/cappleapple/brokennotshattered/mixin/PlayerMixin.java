package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.core.BrokenState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
abstract class PlayerMixin {
    @Redirect(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;getAttackDamageBonus(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)F"
        )
    )
    private float bns$disableSpecialAttackBonus(Item item, Entity target, float damage, DamageSource source) {
        Player player = (Player) (Object) this;
        return BrokenState.isBroken(player.getWeaponItem()) ? 0.0F : item.getAttackDamageBonus(target, damage, source);
    }
    @ModifyExpressionValue(method="attack", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private Item bns$disableSweep(Item original) {
        return BrokenState.isBroken(((Player)(Object)this).getMainHandItem()) ? net.minecraft.world.item.Items.AIR : original;
    }
}
