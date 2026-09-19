package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.client.BrokenIconModel;
import com.cappleapple.brokennotshattered.client.BrokenIconRenderPolicy;
import com.cappleapple.brokennotshattered.client.BrokenAppearance;
import com.cappleapple.brokennotshattered.client.HeldItemWearContext;
import com.cappleapple.brokennotshattered.client.WearPattern;
import com.cappleapple.brokennotshattered.client.WearRenderType;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
abstract class ItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private MultiBufferSource bns$remapOnlyWornTexture(MultiBufferSource buffers) {
        return WearRenderType.textureBuffers(buffers);
    }

    @WrapMethod(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V")
    private void bns$holdWearIdentity(LivingEntity entity, ItemStack stack, ItemDisplayContext context,
                                     boolean leftHand, PoseStack pose, MultiBufferSource buffers, Level level,
                                     int light, int overlay, int seed, Operation<Void> original) {
        try (var ignored = HeldItemWearContext.enter(entity, stack, context)) {
            original.call(entity, stack, context, leftHand, pose, buffers, level, light, overlay, seed);
        }
    }

    @ModifyExpressionValue(
        method = "render",
        at = @At(
            value = "INVOKE",
            remap = false, target = "Lnet/minecraftforge/client/ForgeHooksClient;handleCameraTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemDisplayContext;Z)Lnet/minecraft/client/resources/model/BakedModel;"
        )
    )
    private BakedModel bns$useBrokenAppearance(
        BakedModel resolvedModel,
        ItemStack stack,
        ItemDisplayContext displayContext
    ) {
        ItemStack owner = HeldItemWearContext.identity(stack);
        WearPattern pattern = BrokenAppearance.pattern(owner);
        if (pattern != null && !resolvedModel.isCustomRenderer()) {
            return BrokenIconModel.wrap(resolvedModel, owner, pattern,
                BrokenIconRenderPolicy.shouldSplit(displayContext, resolvedModel),
                displayContext != ItemDisplayContext.GUI);
        }
        return resolvedModel;
    }
}
