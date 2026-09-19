package com.cappleapple.brokennotshattered.mixin;
import java.util.function.Supplier;
import com.mojang.blaze3d.textures.GpuSampler;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets="net.minecraft.client.renderer.rendertype.RenderSetup$TextureBinding")
public interface TextureBindingAccessor {
    @Accessor("location") Identifier bns$location();
    @Accessor("sampler") Supplier<GpuSampler> bns$sampler();
}
