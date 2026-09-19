package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(HumanoidArmorLayer.class)
abstract class HumanoidArmorLayerMixin {
    private ItemStack bns$stack=ItemStack.EMPTY;
    @WrapMethod(method="renderArmorPiece")
    private void bns$stackContext(PoseStack pose,MultiBufferSource buffers,LivingEntity entity,EquipmentSlot slot,int light,HumanoidModel<?> model,Operation<Void> original){
        var previous=bns$stack;bns$stack=entity.getItemBySlot(slot);try{original.call(pose,buffers,entity,slot,light,model);}finally{bns$stack=previous;}
    }
    @ModifyReturnValue(method="getArmorLocation",at=@At("RETURN"))
    private ResourceLocation bns$armorTexture(ResourceLocation source){var pattern=BrokenAppearance.pattern(bns$stack);return pattern==null?source:WornTextures.armor(source,pattern);}
    @WrapMethod(method="renderTrim")
    private void bns$trim(ArmorMaterial material,PoseStack pose,MultiBufferSource buffers,int light,ArmorTrim trim,HumanoidModel<?> model,boolean inner,Operation<Void> original){
        if(!ArmorWear.renderTrim(bns$stack,material,pose,buffers,light,trim,model,inner))original.call(material,pose,buffers,light,trim,model,inner);
    }
}
