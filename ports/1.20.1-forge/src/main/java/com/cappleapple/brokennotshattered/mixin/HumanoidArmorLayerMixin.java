package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.ArmorWear;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidArmorLayer.class)
abstract class HumanoidArmorLayerMixin {
    @Unique private ItemStack bns$armorStack = ItemStack.EMPTY;
    @WrapMethod(method = "renderArmorPiece")
    private void bns$armorContext(PoseStack pose, MultiBufferSource buffers, LivingEntity entity,
                                 EquipmentSlot slot, int light, HumanoidModel<?> model, Operation<Void> original) {
        ItemStack previous = bns$armorStack;
        bns$armorStack = entity.getItemBySlot(slot);
        try { original.call(pose, buffers, entity, slot, light, model); }
        finally { bns$armorStack = previous; }
    }
    @WrapMethod(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V", remap = false)
    private void bns$wearTrim(ArmorMaterial material, PoseStack pose, MultiBufferSource buffers, int light,
                             ArmorTrim trim, Model model, boolean inner, Operation<Void> original) {
        if (!ArmorWear.renderTrim(bns$armorStack, material, pose, buffers, light, trim, model, inner)) {
            original.call(material, pose, buffers, light, trim, model, inner);
        }
    }
}
