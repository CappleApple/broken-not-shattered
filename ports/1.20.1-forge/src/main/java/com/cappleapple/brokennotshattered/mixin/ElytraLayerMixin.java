package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.client.BrokenAppearance;
import com.cappleapple.brokennotshattered.client.WearPattern;
import com.cappleapple.brokennotshattered.client.WornTextures;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ElytraLayer.class)
abstract class ElytraLayerMixin {
    @ModifyArg(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"),
        index = 0
    )
    private ResourceLocation bns$wearElytra(ResourceLocation source, @Local ItemStack stack) {
        WearPattern pattern = BrokenAppearance.pattern(stack);
        return pattern == null ? source : WornTextures.armor(source, pattern);
    }
}
