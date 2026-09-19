package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.ArmorWear;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@org.spongepowered.asm.mixin.Pseudo
@Mixin(targets = "com.bobmowzie.mowziesmobs.client.render.entity.layer.GeckoArmorLayer", remap = false)
abstract class MowzieArmorLayerMixin {
    @Unique private ItemStack bns$mowzieArmorStack = ItemStack.EMPTY;
    @WrapMethod(method = {"renderArmorPiece", "m_117118_"}, remap = false)
    private void bns$mowzieArmorContext(PoseStack pose, MultiBufferSource buffers, LivingEntity entity,
                                 EquipmentSlot slot, int light, HumanoidModel<?> model, Operation<Void> original) {
        ItemStack previous = bns$mowzieArmorStack;
        bns$mowzieArmorStack = entity.getItemBySlot(slot);
        try { original.call(pose, buffers, entity, slot, light, model); }
        finally { bns$mowzieArmorStack = previous; }
    }
    @WrapMethod(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V", remap = false)
    private void bns$wearMowzieTrim(ArmorMaterial material, PoseStack pose, MultiBufferSource buffers, int light,
                             ArmorTrim trim, Model model, boolean inner, Operation<Void> original) {
        if (!ArmorWear.renderTrim(bns$mowzieArmorStack, material, pose, buffers, light, trim, model, inner)) {
            original.call(material, pose, buffers, light, trim, model, inner);
        }
    }
}
