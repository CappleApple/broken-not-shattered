package com.cappleapple.brokennotshattered.mixin;
import com.cappleapple.brokennotshattered.client.WornTextures;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.resources.palette.PalettedTextureManager;
import net.minecraft.client.resources.palette.PaletteMapping;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(PalettedTextureManager.class)
abstract class PalettedTextureManagerMixin {
    @Inject(method="prepareSlot", at=@At(value="RETURN", ordinal=1))
    private void bns$palette(Identifier base, Identifier palette, CallbackInfoReturnable<PalettedTextureManager.Handle> callback,
                            @Local PaletteMapping mapping) {
        WornTextures.capturePalette(callback.getReturnValue(), base, mapping);
    }
}
