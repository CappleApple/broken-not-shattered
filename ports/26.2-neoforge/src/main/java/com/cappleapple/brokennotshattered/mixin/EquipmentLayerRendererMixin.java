package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.*;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
@Mixin(EquipmentLayerRenderer.class)
abstract class EquipmentLayerRendererMixin {
    @ModifyArg(method="renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"), index=0)
    private Identifier bns$material(Identifier texture, @Local(argsOnly=true) ItemStack stack) {
        WearPattern pattern = BrokenAppearance.pattern(stack);
        return pattern == null ? texture : WornTextures.armor(texture, pattern);
    }
    @ModifyArgs(method="renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V", at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"))
    private void bns$trim(Args args, @Local(argsOnly=true) ItemStack stack) {
        TextureAtlasSprite sprite = args.get(7);
        if (sprite == null) return;
        WearPattern pattern = BrokenAppearance.pattern(stack);
        if (pattern == null) return;
        Identifier texture = WornTextures.sprite(sprite, pattern);
        if (texture != null) { args.set(3, WearRenderType.wrap((RenderType)args.get(3), texture)); args.set(7, null); }
    }
}
