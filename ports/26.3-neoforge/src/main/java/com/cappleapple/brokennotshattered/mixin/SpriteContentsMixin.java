package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.WornTextures;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.*;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(SpriteContents.class)
abstract class SpriteContentsMixin implements WornTextures.Source {
    @Unique private Optional<AnimationMetadataSection> bns$animation = Optional.empty();
    @Shadow @Final private NativeImage originalImage;
    @Inject(method="<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V", at=@At("RETURN"))
    private void bns$capture(Identifier id, FrameSize size, NativeImage image, Optional<AnimationMetadataSection> animation, List<MetadataSectionType.WithValue<?>> metadata, Optional<TextureMetadataSection> texture, CallbackInfo ci) { bns$animation = animation; }
    public Optional<AnimationMetadataSection> bns$animation() { return bns$animation; }
    public NativeImage bns$image() { return originalImage; }
}
