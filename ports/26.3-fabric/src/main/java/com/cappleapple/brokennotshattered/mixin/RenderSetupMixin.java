package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.WearRenderType;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Map;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.*;
@Mixin(RenderSetup.class)
abstract class RenderSetupMixin implements WearRenderType.Setup {
    @Shadow @Final RenderPipeline pipeline;
    @Shadow @Final OitPipelineSet oitPipelineSet;
    @Shadow @Final Map<String,Object> textures;
    @Shadow @Final TextureTransform textureTransform;
    @Shadow @Final RenderSetup.OutlineProperty outlineProperty;
    @Shadow @Final String outlineTextureName;
    @Shadow @Final boolean useLightmap;
    @Shadow @Final boolean useOverlay;
    @Shadow @Final boolean affectsCrumbling;
    @Shadow @Final boolean sortOnUpload;
    @Shadow @Final boolean forceSolidModelPhase;
    @Shadow @Final LayeringTransform layeringTransform;
    @Override public RenderSetup bns$withTexture(Identifier texture) {
        var b = RenderSetup.builder(pipeline).setTextureTransform(textureTransform)
            .setOutline(outlineProperty, outlineTextureName).setLayeringTransform(layeringTransform);
        if (oitPipelineSet != null) b.setOitPipelines(oitPipelineSet);
        if (forceSolidModelPhase) b.withForcedSolidModelPhase();
        if(useLightmap) b.useLightmap(); if(useOverlay) b.useOverlay();
        if(affectsCrumbling) b.affectsCrumbling(); if(sortOnUpload) b.sortOnUpload();
        textures.forEach((name, value) -> {
            TextureBindingAccessor binding = (TextureBindingAccessor)value;
            b.withTexture(name, name.equals("Sampler0") ? texture : binding.bns$location(), binding.bns$sampler());
        });
        return b.createRenderSetup();
    }
}
