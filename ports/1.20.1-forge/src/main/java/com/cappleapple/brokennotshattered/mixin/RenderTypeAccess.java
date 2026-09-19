package com.cappleapple.brokennotshattered.mixin;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(RenderType.class)
public interface RenderTypeAccess {
    @Accessor("sortOnUpload") boolean bns$sortOnUpload();
}
