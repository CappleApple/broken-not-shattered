package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.SpriteAnimationMetadata;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.textures.ForgeTextureMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.class)
abstract class SpriteContentsMixin implements SpriteAnimationMetadata {
    @Unique private AnimationMetadataSection bns$metadata = AnimationMetadataSection.EMPTY;
    @Inject(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;Lnet/minecraft/client/resources/metadata/animation/AnimationMetadataSection;Lnet/minecraftforge/client/textures/ForgeTextureMetadata;)V", at = @At("RETURN"))
    private void bns$captureMetadata(ResourceLocation name, FrameSize size, NativeImage image,
                                    AnimationMetadataSection metadata, ForgeTextureMetadata forge, CallbackInfo ci) {
        bns$metadata = metadata;
    }
    @Override public AnimationMetadataSection bns$animationMetadata() { return bns$metadata; }
}
