package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.client.BrokenIconModel;
import com.cappleapple.brokennotshattered.client.BrokenIconRenderPolicy;
import com.cappleapple.brokennotshattered.core.BrokenState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemRenderer.class)
abstract class ItemRendererMixin {
    @ModifyExpressionValue(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/client/ClientHooks;handleCameraTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemDisplayContext;Z)Lnet/minecraft/client/resources/model/BakedModel;"
        )
    )
    private BakedModel bns$useBrokenGuiIcon(
        BakedModel resolvedModel,
        ItemStack stack,
        ItemDisplayContext displayContext
    ) {
        if (
            BrokenState.isBroken(stack)
                && !resolvedModel.isCustomRenderer()
                && BrokenIconRenderPolicy.shouldSplit(displayContext, resolvedModel)
        ) {
            return BrokenIconModel.wrap(resolvedModel, displayContext != ItemDisplayContext.GUI);
        }
        return resolvedModel;
    }
}
