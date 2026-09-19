package com.cappleapple.brokennotshattered.mixin;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.*;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SpriteContents.class)
abstract class SpriteAnimationMixin implements com.cappleapple.brokennotshattered.client.SpriteAnimationMetadata {
    @Unique private AnimationMetadataSection bns$metadata;
    @Inject(method="<init>",at=@At("RETURN"))
    private void bns$remember(ResourceLocation id,FrameSize size,NativeImage image,AnimationMetadataSection metadata,CallbackInfo ci){bns$metadata=metadata;}
    public AnimationMetadataSection bns$animation(){return bns$metadata;}
}
