package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.ModernItemWear;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
@Mixin(ItemFeatureRenderer.class)
abstract class ItemFeatureRendererMixin {
    @ModifyArg(method={"prepareMainSubmit", "prepareOutlineSubmit"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/vertex/VertexConsumer;putBakedQuad(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/resources/model/geometry/BakedQuad;Lcom/mojang/blaze3d/vertex/QuadInstance;)V"), index=1)
    private BakedQuad bns$baseUvs(BakedQuad quad) { return ModernItemWear.baseTextureQuad(quad); }
}
