package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.WearRenderType;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.Map;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.*;
@Mixin(RenderSetup.class)
abstract class RenderSetupMixin implements WearRenderType.Setup {
    @Shadow @Final RenderPipeline pipeline;
    @Shadow @Final Map<String,Object> textures;
    @Shadow @Final TextureTransform textureTransform;
    @Shadow @Final OutputTarget outputTarget;
    @Shadow @Final RenderSetup.OutlineProperty outlineProperty;
    @Shadow @Final boolean useLightmap;
    @Shadow @Final boolean useOverlay;
    @Shadow @Final boolean affectsCrumbling;
    @Shadow @Final boolean sortOnUpload;
    @Shadow @Final LayeringTransform layeringTransform;
    @Override public RenderSetup bns$withTexture(Identifier texture) {
        var b = RenderSetup.builder(pipeline).setTextureTransform(textureTransform).setOutputTarget(outputTarget)
            .setOutline(outlineProperty).setLayeringTransform(layeringTransform);
        if(useLightmap) b.useLightmap(); if(useOverlay) b.useOverlay();
        if(affectsCrumbling) b.affectsCrumbling(); if(sortOnUpload) b.sortOnUpload();
        textures.forEach((name, value) -> {
            TextureBindingAccessor binding = (TextureBindingAccessor)value;
            b.withTexture(name, name.equals("Sampler0") ? texture : binding.bns$location(), binding.bns$sampler());
        });
        return b.createRenderSetup();
    }
}
