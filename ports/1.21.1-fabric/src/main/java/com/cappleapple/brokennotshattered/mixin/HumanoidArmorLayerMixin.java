package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.client.ArmorWear;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HumanoidArmorLayer.class)
abstract class HumanoidArmorLayerMixin {
    @WrapOperation(
        method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V")
    )
    private void bns$wearTrim(HumanoidArmorLayer<?, ?, ?> layer, Holder<ArmorMaterial> material,
                             PoseStack pose, MultiBufferSource buffers, int light, ArmorTrim trim, net.minecraft.client.model.HumanoidModel<?> model,
                             boolean inner, Operation<Void> original, @Local ItemStack stack) {
        if (!ArmorWear.renderTrim(stack, material, pose, buffers, light, trim, model, inner)) {
            original.call(layer, material, pose, buffers, light, trim, model, inner);
        }
    }
    @com.llamalad7.mixinextras.injector.ModifyExpressionValue(method="renderArmorPiece",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ArmorMaterial$Layer;texture(Z)Lnet/minecraft/resources/ResourceLocation;"))
    private net.minecraft.resources.ResourceLocation bns$wearArmor(net.minecraft.resources.ResourceLocation source,@Local ItemStack stack){
        var pattern=com.cappleapple.brokennotshattered.client.BrokenAppearance.pattern(stack);
        return pattern==null?source:com.cappleapple.brokennotshattered.client.WornTextures.armor(source,pattern);
    }
}
