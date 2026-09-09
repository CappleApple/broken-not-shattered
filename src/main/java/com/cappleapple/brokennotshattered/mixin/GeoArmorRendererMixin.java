package com.cappleapple.brokennotshattered.mixin;

import com.cappleapple.brokennotshattered.client.BrokenAppearance;
import com.cappleapple.brokennotshattered.client.WearPattern;
import com.cappleapple.brokennotshattered.client.WornTextures;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** GeckoLib replaces the buffer and texture supplied by the humanoid armor layer. */
@Pseudo
@Mixin(targets = "software.bernie.geckolib.renderer.GeoArmorRenderer", remap = false)
abstract class GeoArmorRendererMixin {
    @Shadow protected ItemStack currentStack;

    @ModifyExpressionValue(
        method = "renderToBuffer",
        at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/renderer/GeoArmorRenderer;getTextureLocation(Lsoftware/bernie/geckolib/animatable/GeoAnimatable;)Lnet/minecraft/resources/ResourceLocation;")
    )
    private ResourceLocation bns$wearGeoArmor(ResourceLocation source) {
        WearPattern pattern = currentStack == null ? null : BrokenAppearance.pattern(currentStack);
        return pattern == null ? source : WornTextures.armor(source, pattern);
    }
}
