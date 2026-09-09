package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.client.ArmorWear;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

/** Mowzie's player layer invokes vanilla trim rendering through its own accessor. */
@Pseudo
@Mixin(targets = "com.bobmowzie.mowziesmobs.client.render.entity.layer.GeckoArmorLayer", remap = false)
abstract class MowzieArmorLayerMixin {
    @WrapOperation(
        method = "renderArmorPiece",
        at = @At(value = "INVOKE", target = "Lcom/bobmowzie/mowziesmobs/mixin/client/HumanoidArmorLayerAccess;mowziesmobs$renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V")
    )
    private void bns$wearTrim(@Coerce Object layer, Holder<ArmorMaterial> material, PoseStack pose,
                             MultiBufferSource buffers, int light, ArmorTrim trim, Model model,
                             boolean inner, Operation<Void> original, @Local ItemStack stack) {
        if (!ArmorWear.renderTrim(stack, material, pose, buffers, light, trim, model, inner)) {
            original.call(layer, material, pose, buffers, light, trim, model, inner);
        }
    }
}
