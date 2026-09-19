package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.core.BrokenState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(Player.class)
abstract class PlayerMixin {
    @ModifyExpressionValue(method="attack",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private Item bns$disableSweep(Item original){return BrokenState.isBroken(((Player)(Object)this).getMainHandItem())?net.minecraft.world.item.Items.AIR:original;}
}
